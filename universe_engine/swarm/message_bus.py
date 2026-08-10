"""Inter-agent message bus - the data pipeline Agent 13 synchronizes.

A small, thread-safe pub/sub bus so the 13 agents can exchange telemetry,
faults, and state deltas without shared mutable references. The conductor
flushes a snapshot of the bus into the persisted WorldState each tick.
"""
from __future__ import annotations

import threading
import time
from collections import deque
from dataclasses import dataclass, asdict
from typing import Any, Callable, Deque, Dict, List, Optional


@dataclass
class Message:
    src: int                 # agent id
    dst: int                 # agent id (0 = broadcast)
    topic: str
    payload: Any
    ts: float

    def to_dict(self) -> Dict[str, Any]:
        return asdict(self)


class MessageBus:
    def __init__(self, maxlen: int = 4096):
        self._lock = threading.RLock()
        self._queue: Deque[Message] = deque(maxlen=maxlen)
        self._subs: Dict[str, List[Callable[[Message], None]]] = {}
        self._count = 0

    def publish(self, src: int, dst: int, topic: str, payload: Any) -> Message:
        m = Message(src, dst, topic, payload, time.time())
        with self._lock:
            self._queue.append(m)
            self._count += 1
            subs = list(self._subs.get(topic, [])) + list(self._subs.get("*", []))
        for cb in subs:
            try:
                cb(m)
            except Exception:
                pass        # never let a subscriber kill the pipeline
        return m

    def subscribe(self, topic: str, cb: Callable[[Message], None]) -> None:
        with self._lock:
            self._subs.setdefault(topic, []).append(cb)

    def drain(self, limit: int = 512) -> List[Message]:
        with self._lock:
            out = []
            while self._queue and len(out) < limit:
                out.append(self._queue.popleft())
            return out

    def snapshot(self, limit: int = 64) -> List[Dict[str, Any]]:
        with self._lock:
            return [m.to_dict() for m in list(self._queue)[-limit:]]

    @property
    def total_published(self) -> int:
        with self._lock:
            return self._count
