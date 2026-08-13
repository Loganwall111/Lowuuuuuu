/**
 * RenderWatchdog — catches a black screen and says why.
 *
 * A black canvas is the worst failure this app has, because every layer
 * above it looks healthy: init() resolves, the DOM is correct, the scene
 * has meshes, and the tests pass. The failure lives in WebGL, which the
 * jsdom test harness cannot execute.
 *
 * So instead of trying to predict the cause, this watches the real canvas
 * at runtime, reads pixels back off the GPU, and if the frame is genuinely
 * blank it reports the specific reason on screen rather than leaving the
 * user staring at nothing.
 *
 * It runs a handful of times just after boot and then stops, so it costs
 * nothing during play.
 */

export interface WatchdogReport {
  /** True if the canvas is drawing something. */
  painting: boolean;
  /** Mean luminance 0..1 of the sampled pixels. */
  luminance: number;
  /** Human-readable diagnosis. */
  diagnosis: string;
  /** Things worth reporting even if the screen is fine. */
  warnings: string[];
}

export interface WatchdogDeps {
  canvas: HTMLCanvasElement;
  /** WebGL context, if one was obtained. */
  gl: WebGL2RenderingContext | WebGLRenderingContext | null | undefined;
  /** Number of meshes currently in the scene. */
  meshCount: () => number;
  /** Number of frame errors seen so far. */
  frameErrors: () => number;
  /** First frame error message, if any. */
  firstError: () => string;
  /** How many frames the engine believes it has drawn. */
  fps: () => number;
}

/**
 * Samples the canvas and decides whether anything is actually being drawn.
 *
 * Reading pixels back is the only honest test: everything else is a proxy.
 * `preserveDrawingBuffer` is already enabled on the engine, so this is safe
 * to call outside the draw call.
 */
export function inspectFrame(deps: WatchdogDeps): WatchdogReport {
  const warnings: string[] = [];
  const { canvas, gl } = deps;

  // ---- structural checks first: these explain most black screens ----
  if (!canvas) {
    return { painting: false, luminance: 0, warnings,
             diagnosis: 'No canvas element found in the page.' };
  }

  const w = canvas.width;
  const h = canvas.height;
  if (!w || !h) {
    return {
      painting: false, luminance: 0, warnings,
      diagnosis: `The canvas has zero size (${w}x${h}). Nothing can be drawn ` +
                 'into it. Usually a CSS layout problem, not a graphics one.'
    };
  }

  const rect = typeof canvas.getBoundingClientRect === 'function'
    ? canvas.getBoundingClientRect() : null;
  if (rect && (rect.width < 2 || rect.height < 2)) {
    return {
      painting: false, luminance: 0, warnings,
      diagnosis: `The canvas is laid out at ${Math.round(rect.width)}x` +
                 `${Math.round(rect.height)} CSS pixels, so it is effectively ` +
                 'invisible even though the drawing buffer is valid.'
    };
  }

  if (!gl) {
    return {
      painting: false, luminance: 0, warnings,
      diagnosis: 'No WebGL context. The browser or driver refused one, so ' +
                 'nothing can render at all.'
    };
  }

  if (typeof gl.isContextLost === 'function' && gl.isContextLost()) {
    return {
      painting: false, luminance: 0, warnings,
      diagnosis: 'The WebGL context was lost. This is usually a GPU driver ' +
                 'reset or the tab being starved of resources.'
    };
  }

  // ---- read the framebuffer ----
  // A sparse grid rather than every pixel: enough to tell black from
  // not-black, cheap enough to run mid-frame.
  const COLS = 12, ROWS = 8;
  let total = 0, samples = 0, maxLum = 0;
  const px = new Uint8Array(4);

  try {
    for (let iy = 0; iy < ROWS; iy++) {
      for (let ix = 0; ix < COLS; ix++) {
        const x = Math.floor(((ix + 0.5) / COLS) * w);
        const y = Math.floor(((iy + 0.5) / ROWS) * h);
        gl.readPixels(x, y, 1, 1, gl.RGBA, gl.UNSIGNED_BYTE, px);
        // Rec. 709 luma, close enough for "is anything there".
        const lum = (0.2126 * px[0] + 0.7152 * px[1] + 0.0722 * px[2]) / 255;
        total += lum;
        maxLum = Math.max(maxLum, lum);
        samples++;
      }
    }
  } catch (e) {
    return {
      painting: false, luminance: 0, warnings,
      diagnosis: 'Could not read pixels back from the GPU: ' + String(e)
    };
  }

  const luminance = samples > 0 ? total / samples : 0;

  // Deep space is legitimately very dark, so the threshold has to be low.
  // A truly black frame reads 0.000 everywhere; a starfield does not.
  const painting = maxLum > 0.012 || luminance > 0.004;

  if (deps.frameErrors() > 0) {
    warnings.push(`${deps.frameErrors()} frame error(s): ${deps.firstError()}`);
  }
  if (deps.meshCount() === 0) {
    warnings.push('The scene contains no meshes.');
  }

  let diagnosis: string;
  if (painting) {
    diagnosis = 'Rendering normally.';
  } else if (deps.frameErrors() > 0) {
    diagnosis =
      'The screen is black because the render loop is throwing. First ' +
      'error: ' + deps.firstError();
  } else if (deps.meshCount() === 0) {
    diagnosis =
      'The screen is black because the scene has no meshes - the world ' +
      'failed to build.';
  } else if (deps.fps() < 1) {
    diagnosis =
      'The screen is black and no frames are completing. The render loop ' +
      'is not running.';
  } else {
    diagnosis =
      `The canvas is drawing (${deps.fps().toFixed(0)} fps, ` +
      `${deps.meshCount()} meshes) but every sampled pixel is black. The ` +
      'camera is probably facing away from the scene, or a post-process is ' +
      'writing black over the frame.';
  }

  return { painting, luminance, diagnosis, warnings };
}

/**
 * Shows a dismissible panel explaining a black screen. Deliberately styled
 * inline so it works even if the stylesheet is what broke.
 */
export function showBlackScreenReport(report: WatchdogReport): void {
  if (typeof document === 'undefined') return;
  if (document.getElementById('blackScreenReport')) return;

  const el = document.createElement('div');
  el.id = 'blackScreenReport';
  el.style.cssText = [
    'position:fixed', 'left:50%', 'top:50%', 'transform:translate(-50%,-50%)',
    'z-index:9999', 'max-width:min(560px,90vw)', 'padding:20px 22px',
    'background:#141a26', 'color:#eaf0ff', 'border:1px solid #4d7fd0',
    'border-radius:12px', 'font:13px/1.6 Inter,system-ui,sans-serif',
    'box-shadow:0 20px 70px rgba(0,0,0,.7)'
  ].join(';');

  const warn = report.warnings.length
    ? `<ul style="margin:10px 0 0;padding-left:18px;color:#ffcf9a">` +
      report.warnings.map((w) => `<li>${w}</li>`).join('') + '</ul>'
    : '';

  el.innerHTML = `
    <div style="font-size:15px;font-weight:700;color:#7fb2ff;margin-bottom:8px">
      The screen is black
    </div>
    <div>${report.diagnosis}</div>
    ${warn}
    <div style="margin-top:12px;font-size:11.5px;color:#93a4c4">
      Mean luminance ${report.luminance.toFixed(4)}. Full details are in the
      browser console.
    </div>
    <button id="bsrClose" style="margin-top:14px;padding:8px 18px;cursor:pointer;
      background:#2f6fd0;color:#fff;border:0;border-radius:8px;font:inherit">
      Dismiss
    </button>`;

  document.body.appendChild(el);
  const btn = el.querySelector('#bsrClose') as HTMLButtonElement | null;
  if (btn) btn.onclick = () => el.remove();
}
