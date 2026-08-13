/**
 * SafeUniforms — values that cannot poison a shader.
 *
 * A single NaN reaching a shader uniform is catastrophic and silent. NaN
 * propagates through every arithmetic operation it touches, so one bad
 * value in a ray direction turns the entire frame black with no error
 * anywhere - which is exactly the failure this module exists to stop.
 *
 * The usual source is not exotic maths. It is a division whose denominator
 * is legitimately zero for one frame: a canvas mid-resize has zero height,
 * so width/height is 0/0, and the aspect ratio arrives as NaN. Opening a
 * panel is enough to trigger it.
 *
 * These helpers are deliberately dull. They are meant to be used at every
 * boundary where a number enters a shader, so that no individual call site
 * has to be clever.
 */

/**
 * A finite number, or the fallback.
 *
 * Rejects NaN and both infinities. Infinity is just as fatal as NaN in a
 * shader: it survives multiplication and turns comparisons false.
 */
export function safeFloat(v: number, fallback: number): number {
  return Number.isFinite(v) ? v : fallback;
}

/**
 * Division that cannot produce NaN or Infinity.
 *
 * Used instead of `a / b` wherever `b` comes from measured state - canvas
 * size, distance, elapsed time - rather than from a constant.
 */
export function safeDiv(a: number, b: number, fallback: number): number {
  if (!Number.isFinite(a) || !Number.isFinite(b) || Math.abs(b) < 1e-12) {
    return fallback;
  }
  const r = a / b;
  return Number.isFinite(r) ? r : fallback;
}

/**
 * Aspect ratio of a render surface.
 *
 * 16:9 is the fallback because a wrong-but-plausible aspect renders a
 * slightly stretched frame the user can still see and still play through,
 * whereas NaN renders nothing at all. Failing visibly beats failing black.
 */
export function safeAspect(width: number, height: number): number {
  // A dimension that is not a sane positive number means the canvas is not
  // really laid out yet (collapsed, hidden, mid-reflow). Clamping such a
  // value to 1 would "work" but yields a nonsense ratio - 1920x0 would
  // become 1920:1 and stretch the frame into unreadable streaks. Falling
  // back to 16:9 renders a correct-looking frame instead, and the next
  // resize with real numbers corrects it.
  const okW = Number.isFinite(width) && width >= 1;
  const okH = Number.isFinite(height) && height >= 1;
  if (!okW || !okH) return 16 / 9;

  // Both dimensions are sane, so the division cannot produce NaN or
  // Infinity. safeDiv stays as a second line of defence.
  const a = safeDiv(width, height, 16 / 9);
  // Clamp absurd ratios too: a 1x2000 sliver is not worth rendering and
  // produces extreme ray divergence.
  return Math.max(0.05, Math.min(20, a));
}

/** Clamps into a range, substituting the midpoint for non-finite input. */
export function safeClamp(v: number, lo: number, hi: number): number {
  if (!Number.isFinite(v)) return (lo + hi) * 0.5;
  return Math.max(lo, Math.min(hi, v));
}

/**
 * Normalises a 3-vector without dividing by zero.
 *
 * Returns the supplied default direction for a zero-length vector, which is
 * the case that would otherwise yield NaN in all three components.
 */
export function safeNormalize(
  x: number, y: number, z: number,
  dx = 0, dy = 0, dz = 1
): [number, number, number] {
  const len2 = x * x + y * y + z * z;
  if (!Number.isFinite(len2) || len2 < 1e-24) return [dx, dy, dz];
  const inv = 1 / Math.sqrt(len2);
  const r: [number, number, number] = [x * inv, y * inv, z * inv];
  return r.every(Number.isFinite) ? r : [dx, dy, dz];
}
