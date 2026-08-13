/**
 * Input Manager - Handles all user input with proper state management
 */

export interface PointerState {
  x: number;
  y: number;
  deltaX: number;
  deltaY: number;
  isDown: boolean;
  isLeftDown: boolean;
  isRightDown: boolean;
  isMiddleDown: boolean;
  clickCount: number;
}

export interface KeyboardState {
  keys: Set<string>;
  justPressed: Set<string>;
  justReleased: Set<string>;
  shift: boolean;
  ctrl: boolean;
  alt: boolean;
  meta: boolean;
}

export class InputManager {
  private canvas: HTMLCanvasElement | null = null;
  private pointerState: PointerState = {
    x: 0,
    y: 0,
    deltaX: 0,
    deltaY: 0,
    isDown: false,
    isLeftDown: false,
    isRightDown: false,
    isMiddleDown: false,
    clickCount: 0
  };
  private previousPointerState: PointerState = { ...this.pointerState };
  private keyboardState: KeyboardState = {
    keys: new Set(),
    justPressed: new Set(),
    justReleased: new Set(),
    shift: false,
    ctrl: false,
    alt: false,
    meta: false
  };
  private lastClickTime = 0;
  private isPointerCaptured = false;
  private captureElement: HTMLElement | null = null;
  
  // Event callbacks
  private callbacks = {
    onPointerDown: [] as ((x: number, y: number, button: number) => void)[],
    onPointerUp: [] as ((x: number, y: number, button: number) => void)[],
    onPointerMove: [] as ((x: number, y: number, deltaX: number, deltaY: number) => void)[],
    onPointerWheel: [] as ((deltaX: number, deltaY: number) => void)[],
    onKeyDown: [] as ((key: string, code: string) => void)[],
    onKeyUp: [] as ((key: string, code: string) => void)[],
    onClick: [] as ((x: number, y: number) => void)[],
    onDoubleClick: [] as ((x: number, y: number) => void)[],
    onContextMenu: [] as ((x: number, y: number) => void)[],
  };

  private toolSystem: any = null;
  private cameraRef: any = null;

  // App.ts constructs this as: new InputManager(canvas, camera)
  constructor(canvas?: HTMLCanvasElement, camera?: any) {
    if (camera) this.cameraRef = camera;
    if (canvas) this.setCanvas(canvas);
  }

  setToolSystem(toolSystem: any): void {
    this.toolSystem = toolSystem;
  }

  setCanvas(canvas: HTMLCanvasElement | null): void {
    if (this.canvas) {
      this.removeListeners(this.canvas);
    }
    
    this.canvas = canvas;
    
    if (canvas) {
      this.addListeners(canvas);
    }
  }

  private addListeners(canvas: HTMLCanvasElement): void {
    canvas.addEventListener('mousedown', this.handleMouseDown.bind(this));
    canvas.addEventListener('mousemove', this.handleMouseMove.bind(this));
    canvas.addEventListener('mouseup', this.handleMouseUp.bind(this));
    canvas.addEventListener('mouseleave', this.handleMouseLeave.bind(this));
    canvas.addEventListener('wheel', this.handleWheel.bind(this), { passive: false });
    canvas.addEventListener('click', this.handleClick.bind(this));
    canvas.addEventListener('dblclick', this.handleDoubleClick.bind(this));
    canvas.addEventListener('contextmenu', this.handleContextMenu.bind(this));
    canvas.addEventListener('keydown', this.handleKeyDown.bind(this));
    canvas.addEventListener('keyup', this.handleKeyUp.bind(this));
    
    // Prevent default context menu
    canvas.addEventListener('contextmenu', (e) => e.preventDefault());
    
    // Set up pointer capture for better input handling
    canvas.addEventListener('pointerdown', (e) => {
      try {
        canvas.setPointerCapture(e.pointerId);
        this.isPointerCaptured = true;
        this.captureElement = canvas;
      } catch (e) {
        // Capture may fail in some contexts
      }
    });
  }

  private removeListeners(canvas: HTMLCanvasElement): void {
    canvas.removeEventListener('mousedown', this.handleMouseDown.bind(this));
    canvas.removeEventListener('mousemove', this.handleMouseMove.bind(this));
    canvas.removeEventListener('mouseup', this.handleMouseUp.bind(this));
    canvas.removeEventListener('mouseleave', this.handleMouseLeave.bind(this));
    canvas.removeEventListener('wheel', this.handleWheel.bind(this));
    canvas.removeEventListener('click', this.handleClick.bind(this));
    canvas.removeEventListener('dblclick', this.handleDoubleClick.bind(this));
    canvas.removeEventListener('contextmenu', this.handleContextMenu.bind(this));
    canvas.removeEventListener('keydown', this.handleKeyDown.bind(this));
    canvas.removeEventListener('keyup', this.handleKeyUp.bind(this));
  }

  private handleMouseDown(e: MouseEvent): void {
    const rect = this.getCanvasRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    this.pointerState.isDown = true;
    this.pointerState.isLeftDown = e.button === 0;
    this.pointerState.isRightDown = e.button === 2;
    this.pointerState.isMiddleDown = e.button === 1;
    this.pointerState.x = x;
    this.pointerState.y = y;

    // Calculate click count for double/triple click detection
    const now = Date.now();
    if (now - this.lastClickTime < 300) {
      this.pointerState.clickCount++;
    } else {
      this.pointerState.clickCount = 1;
    }
    this.lastClickTime = now;

    // Trigger callbacks
    this.callbacks.onPointerDown.forEach(cb => cb(x, y, e.button));
    this.callbacks.onClick.forEach(cb => cb(x, y));
  }

  private handleMouseUp(e: MouseEvent): void {
    const rect = this.getCanvasRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    this.pointerState.isDown = false;
    this.pointerState.isLeftDown = false;
    this.pointerState.isRightDown = false;
    this.pointerState.isMiddleDown = false;
    this.pointerState.x = x;
    this.pointerState.y = y;

    // Release pointer capture
    if (this.isPointerCaptured && this.captureElement) {
      try {
        this.captureElement.releasePointerCapture((e as PointerEvent).pointerId);
      } catch (e) {
        // Ignore
      }
      this.isPointerCaptured = false;
      this.captureElement = null;
    }

    this.callbacks.onPointerUp.forEach(cb => cb(x, y, e.button));
  }

  private handleMouseMove(e: MouseEvent): void {
    const rect = this.getCanvasRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    this.pointerState.deltaX = x - this.pointerState.x;
    this.pointerState.deltaY = y - this.pointerState.y;
    this.pointerState.x = x;
    this.pointerState.y = y;

    this.callbacks.onPointerMove.forEach(cb => cb(x, y, this.pointerState.deltaX, this.pointerState.deltaY));
  }

  private handleMouseLeave(e: MouseEvent): void {
    this.pointerState.isDown = false;
    this.pointerState.isLeftDown = false;
    this.pointerState.isRightDown = false;
    this.pointerState.isMiddleDown = false;
  }

  private handleWheel(e: WheelEvent): void {
    e.preventDefault();
    this.callbacks.onPointerWheel.forEach(cb => cb(e.deltaX, e.deltaY));
  }

  private handleClick(e: MouseEvent): void {
    const rect = this.getCanvasRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    this.callbacks.onClick.forEach(cb => cb(x, y));
  }

  private handleDoubleClick(e: MouseEvent): void {
    const rect = this.getCanvasRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    this.callbacks.onDoubleClick.forEach(cb => cb(x, y));
  }

  private handleContextMenu(e: MouseEvent): void {
    e.preventDefault();
    const rect = this.getCanvasRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    this.callbacks.onContextMenu.forEach(cb => cb(x, y));
  }

  private handleKeyDown(e: KeyboardEvent): void {
    const key = e.key.toLowerCase();
    const code = e.code;

    if (!this.keyboardState.keys.has(key)) {
      this.keyboardState.justPressed.add(key);
    }
    this.keyboardState.keys.add(key);

    this.keyboardState.shift = e.shiftKey;
    this.keyboardState.ctrl = e.ctrlKey;
    this.keyboardState.alt = e.altKey;
    this.keyboardState.meta = e.metaKey;

    this.callbacks.onKeyDown.forEach(cb => cb(key, code));
    
    // Prevent default for game keys
    if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', ' ', 'Tab'].includes(e.key)) {
      e.preventDefault();
    }
  }

  private handleKeyUp(e: KeyboardEvent): void {
    const key = e.key.toLowerCase();
    const code = e.code;

    this.keyboardState.keys.delete(key);
    this.keyboardState.justReleased.add(key);

    this.callbacks.onKeyUp.forEach(cb => cb(key, code));
  }

  private getCanvasRect(): DOMRect {
    if (!this.canvas) {
      return new DOMRect(0, 0, window.innerWidth, window.innerHeight);
    }
    return this.canvas.getBoundingClientRect();
  }

  // Update - call this each frame to clear justPressed/justReleased
  update(_delta?: number): void {
    this.previousPointerState = { ...this.pointerState };
    this.keyboardState.justPressed.clear();
    this.keyboardState.justReleased.clear();
  }

  // Pointer state accessors
  getPointerX(): number { return this.pointerState.x; }
  getPointerY(): number { return this.pointerState.y; }
  getPointerDeltaX(): number { return this.pointerState.deltaX; }
  getPointerDeltaY(): number { return this.pointerState.deltaY; }
  isPointerDown(): boolean { return this.pointerState.isDown; }
  isLeftDown(): boolean { return this.pointerState.isLeftDown; }
  isRightDown(): boolean { return this.pointerState.isRightDown; }
  isMiddleDown(): boolean { return this.pointerState.isMiddleDown; }
  getClickCount(): number { return this.pointerState.clickCount; }

  // Keyboard state accessors
  isKeyDown(key: string): boolean {
    return this.keyboardState.keys.has(key.toLowerCase());
  }
  isKeyJustPressed(key: string): boolean {
    return this.keyboardState.justPressed.has(key.toLowerCase());
  }
  isKeyJustReleased(key: string): boolean {
    return this.keyboardState.justReleased.has(key.toLowerCase());
  }
  isShiftDown(): boolean { return this.keyboardState.shift; }
  isCtrlDown(): boolean { return this.keyboardState.ctrl; }
  isAltDown(): boolean { return this.keyboardState.alt; }
  isMetaDown(): boolean { return this.keyboardState.meta; }

  // Check for ESC key
  isEscapePressed(): boolean {
    return this.keyboardState.justPressed.has('escape');
  }

  // Get combined position
  getPointerPosition(): { x: number; y: number } {
    return { x: this.pointerState.x, y: this.pointerState.y };
  }

  // Get combined delta
  getPointerDelta(): { x: number; y: number } {
    return { x: this.pointerState.deltaX, y: this.pointerState.deltaY };
  }

  // Add event callbacks
  onPointerDown(callback: (x: number, y: number, button: number) => void): void {
    this.callbacks.onPointerDown.push(callback);
  }

  onPointerUp(callback: (x: number, y: number, button: number) => void): void {
    this.callbacks.onPointerUp.push(callback);
  }

  onPointerMove(callback: (x: number, y: number, deltaX: number, deltaY: number) => void): void {
    this.callbacks.onPointerMove.push(callback);
  }

  onPointerWheel(callback: (deltaX: number, deltaY: number) => void): void {
    this.callbacks.onPointerWheel.push(callback);
  }

  onKeyDown(callback: (key: string, code: string) => void): void {
    this.callbacks.onKeyDown.push(callback);
  }

  onKeyUp(callback: (key: string, code: string) => void): void {
    this.callbacks.onKeyUp.push(callback);
  }

  onClick(callback: (x: number, y: number) => void): void {
    this.callbacks.onClick.push(callback);
  }

  onDoubleClick(callback: (x: number, y: number) => void): void {
    this.callbacks.onDoubleClick.push(callback);
  }

  onContextMenu(callback: (x: number, y: number) => void): void {
    this.callbacks.onContextMenu.push(callback);
  }

  // Remove event callbacks
  offPointerDown(callback: (x: number, y: number, button: number) => void): void {
    this.callbacks.onPointerDown = this.callbacks.onPointerDown.filter(cb => cb !== callback);
  }

  offPointerUp(callback: (x: number, y: number, button: number) => void): void {
    this.callbacks.onPointerUp = this.callbacks.onPointerUp.filter(cb => cb !== callback);
  }

  offPointerMove(callback: (x: number, y: number, deltaX: number, deltaY: number) => void): void {
    this.callbacks.onPointerMove = this.callbacks.onPointerMove.filter(cb => cb !== callback);
  }

  offPointerWheel(callback: (deltaX: number, deltaY: number) => void): void {
    this.callbacks.onPointerWheel = this.callbacks.onPointerWheel.filter(cb => cb !== callback);
  }

  offKeyDown(callback: (key: string, code: string) => void): void {
    this.callbacks.onKeyDown = this.callbacks.onKeyDown.filter(cb => cb !== callback);
  }

  offKeyUp(callback: (key: string, code: string) => void): void {
    this.callbacks.onKeyUp = this.callbacks.onKeyUp.filter(cb => cb !== callback);
  }

  offClick(callback: (x: number, y: number) => void): void {
    this.callbacks.onClick = this.callbacks.onClick.filter(cb => cb !== callback);
  }

  offDoubleClick(callback: (x: number, y: number) => void): void {
    this.callbacks.onDoubleClick = this.callbacks.onDoubleClick.filter(cb => cb !== callback);
  }

  offContextMenu(callback: (x: number, y: number) => void): void {
    this.callbacks.onContextMenu = this.callbacks.onContextMenu.filter(cb => cb !== callback);
  }

  // Capture pointer to this input manager
  capturePointer(element: HTMLElement): void {
    this.captureElement = element;
    this.isPointerCaptured = true;
    try {
      element.setPointerCapture(0);
    } catch (e) {
      // May fail
    }
  }

  // Release pointer capture
  releasePointerCapture(): void {
    if (this.captureElement) {
      try {
        this.captureElement.releasePointerCapture(0);
      } catch (e) {
        // Ignore
      }
    }
    this.captureElement = null;
    this.isPointerCaptured = false;
  }

  // Clear all callbacks
  clearCallbacks(): void {
    this.callbacks = {
      onPointerDown: [],
      onPointerUp: [],
      onPointerMove: [],
      onPointerWheel: [],
      onKeyDown: [],
      onKeyUp: [],
      onClick: [],
      onDoubleClick: [],
      onContextMenu: [],
    };
  }

  // Reset input state
  reset(): void {
    this.pointerState = {
      x: 0,
      y: 0,
      deltaX: 0,
      deltaY: 0,
      isDown: false,
      isLeftDown: false,
      isRightDown: false,
      isMiddleDown: false,
      clickCount: 0
    };
    this.keyboardState = {
      keys: new Set(),
      justPressed: new Set(),
      justReleased: new Set(),
      shift: false,
      ctrl: false,
      alt: false,
      meta: false
    };
  }

  // Focus handling - restore focus after window closes
  restoreFocus(focusElement: HTMLElement | null): void {
    if (focusElement) {
      focusElement.focus();
    } else if (this.canvas) {
      this.canvas.focus();
    }
  }
}
