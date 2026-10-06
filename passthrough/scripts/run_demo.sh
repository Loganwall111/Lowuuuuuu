#!/usr/bin/env bash
# SIFT Bridge sandbox demo: fake Dungeons II host + fake Minecraft guest, both speaking the real protocol.
# Preview: http://0.0.0.0:8081 (this box) — the live compositing view.
set -euo pipefail
cd "$(dirname "$0")/.."

cleanup() {
  [[ -n "${GUEST_PID:-}" ]] && kill "$GUEST_PID" 2>/dev/null || true
  [[ -n "${HOST_PID:-}" ]] && kill "$HOST_PID" 2>/dev/null || true
}
trap cleanup EXIT

python3 harness/fake_host.py --port "${SIFT_PORT:-8080}" --preview-port "${SIFT_PREVIEW:-8081}" &
HOST_PID=$!
sleep 1
python3 harness/fake_guest.py &
GUEST_PID=$!

echo
echo "  SIFT Bridge demo running."
echo "    link:    ws://127.0.0.1:${SIFT_PORT:-8080}"
echo "    preview: http://localhost:${SIFT_PREVIEW:-8081}"
echo "  Press Ctrl-C to stop (the guest says bye and unlinks the ring)."
wait
