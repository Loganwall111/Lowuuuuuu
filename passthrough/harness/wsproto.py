"""Minimal RFC 6455 WebSocket layer over asyncio streams — stdlib only.

Just enough for the SIFT Bridge link: text frames, close, ping/pong, masking.
Both the fake host (server) and the fake guest (client) speak this, mirroring
what Java-WebSocket (guest) and the winsock WS server (UE module) do in production.
"""
from __future__ import annotations

import asyncio
import base64
import hashlib
import os

GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"


async def _read_exact(reader: asyncio.StreamReader, n: int) -> bytes:
    data = b""
    while len(data) < n:
        chunk = await reader.read(n - len(data))
        if not chunk:
            raise ConnectionError("peer closed")
        data += chunk
    return data


def _frame(opcode: int, payload: bytes, mask: bool) -> bytes:
    header = bytes([0x80 | opcode])
    ln = len(payload)
    mask_bit = 0x80 if mask else 0
    if ln < 126:
        header += bytes([mask_bit | ln])
    elif ln < 1 << 16:
        header += bytes([mask_bit | 126]) + ln.to_bytes(2, "big")
    else:
        header += bytes([mask_bit | 127]) + ln.to_bytes(8, "big")
    if mask:
        key = os.urandom(4)
        masked = bytes(b ^ key[i & 3] for i, b in enumerate(payload))
        return header + key + masked
    return header + payload


async def send_text(writer: asyncio.StreamWriter, text: str, mask: bool) -> None:
    writer.write(_frame(0x1, text.encode("utf-8"), mask))
    await writer.drain()


async def send_close(writer: asyncio.StreamWriter, mask: bool) -> None:
    try:
        writer.write(_frame(0x8, b"", mask))
        await writer.drain()
    except (ConnectionError, RuntimeError):
        pass


async def read_message(reader: asyncio.StreamReader) -> str | None:
    """Next text message, or None on close."""
    while True:
        hdr = await _read_exact(reader, 2)
        opcode = hdr[0] & 0x0F
        masked = bool(hdr[1] & 0x80)
        ln = hdr[1] & 0x7F
        if ln == 126:
            ln = int.from_bytes(await _read_exact(reader, 2), "big")
        elif ln == 127:
            ln = int.from_bytes(await _read_exact(reader, 8), "big")
        key = await _read_exact(reader, 4) if masked else None
        payload = await _read_exact(reader, ln) if ln else b""
        if key:
            payload = bytes(b ^ key[i & 3] for i, b in enumerate(payload))
        if opcode == 0x8:
            return None
        if opcode == 0x9:  # ping → pong is handled by the caller's writer
            continue
        if opcode in (0x1, 0x0):
            return payload.decode("utf-8", "replace")


# ------------------------------------------------------------------ HTTP upgrade

def _accept_value(key: str) -> str:
    return base64.b64encode(hashlib.sha1((key + GUID).encode()).digest()).decode()


async def server_handshake(reader: asyncio.StreamReader, writer: asyncio.StreamWriter) -> None:
    headers = b""
    while b"\r\n\r\n" not in headers:
        chunk = await reader.read(4096)
        if not chunk:
            raise ConnectionError("upgrade aborted")
        headers += chunk
        if len(headers) > 1 << 16:
            raise ValueError("oversized upgrade request")
    key = ""
    for line in headers.decode("latin-1").split("\r\n"):
        if line.lower().startswith("sec-websocket-key:"):
            key = line.split(":", 1)[1].strip()
    if not key:
        raise ValueError("missing Sec-WebSocket-Key")
    writer.write((
        "HTTP/1.1 101 Switching Protocols\r\n"
        "Upgrade: websocket\r\n"
        "Connection: Upgrade\r\n"
        f"Sec-WebSocket-Accept: {_accept_value(key)}\r\n\r\n"
    ).encode())
    await writer.drain()


async def client_handshake(reader: asyncio.StreamReader, writer: asyncio.StreamWriter, host: str, port: int) -> None:
    key = base64.b64encode(os.urandom(16)).decode()
    writer.write((
        f"GET / HTTP/1.1\r\nHost: {host}:{port}\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n"
        f"Sec-WebSocket-Key: {key}\r\nSec-WebSocket-Version: 13\r\n\r\n"
    ).encode())
    await writer.drain()
    resp = b""
    while b"\r\n\r\n" not in resp:
        chunk = await reader.read(4096)
        if not chunk:
            raise ConnectionError("upgrade rejected")
        resp += chunk
    if b"101" not in resp.split(b"\r\n", 1)[0]:
        raise ConnectionError(f"upgrade refused: {resp[:80]!r}")
