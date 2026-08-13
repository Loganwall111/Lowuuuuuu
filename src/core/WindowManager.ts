/**
 * Window Manager - Centralized window management system
 * 
 * CRITICAL: This system must handle all UI windows to prevent:
 * - Black screen/panel states
 * - Dead close buttons
 * - Input capture issues
 * - Stuck overlays
 * 
 * Every window supports: Open, Close, Toggle, Minimize, Maximize, BringToFront, SendToBack, Reset, IsOpen, IsVisible
 */

export interface WindowConfig {
  id: string;
  title?: string;
  width?: number;
  height?: number;
  minWidth?: number;
  minHeight?: number;
  maxWidth?: number;
  maxHeight?: number;
  position?: { x: number; y: number };
  initPosition?: 'center' | 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right';
  draggable?: boolean;
  resizable?: boolean;
  minimizable?: boolean;
  maximizable?: boolean;
  closable?: boolean;
  modal?: boolean;
  persistent?: boolean;
  closeOnEscape?: boolean;
  closeOnOutsideClick?: boolean;
  showContent?: boolean;
  type?: 'panel' | 'dialog' | 'popup' | 'tool' | 'overlay' | 'widget';
}

export interface WindowState {
  isOpen: boolean;
  isVisible: boolean;
  isMinimized: boolean;
  isMaximized: boolean;
  position: { x: number; y: number };
  size: { width: number; height: number };
  zIndex: number;
}

export class WindowManager {
  private windows: Map<string, {
    config: WindowConfig;
    state: WindowState;
    element: HTMLElement | null;
    content: HTMLElement | null;
    controls: {
      titleBar: HTMLElement | null;
      closeButton: HTMLElement | null;
      minimizeButton: HTMLElement | null;
      maximizeButton: HTMLElement | null;
      resetButton: HTMLElement | null;
    };
    handlers: {
      onClose: (() => void) | null;
      onMinimize: (() => void) | null;
      onMaximize: (() => void) | null;
      onReset: (() => void) | null;
      onBringToFront: (() => void) | null;
    };
  }> = new Map();

  private uiContainer: HTMLElement | null = null;
  private nextZIndex = 1000;
  private activeWindowId: string | null = null;
  private previousWindowId: string | null = null;

  eventListeners: {
    onWindowOpen: ((id: string) => void) | null;
    onWindowClose: ((id: string) => void) | null;
    onWindowFocus: ((id: string) => void) | null;
    onActiveWindowChange: ((id: string | null) => void) | null;
  } = {
    onWindowOpen: null,
    onWindowClose: null,
    onWindowFocus: null,
    onActiveWindowChange: null
  };

  async initialize(): Promise<void> {
    console.log('WindowManager initialized');
  }

  setUIContainer(container: HTMLElement): void {
    this.uiContainer = container;
  }

  // Called by App.ts: bind the HUD layer and wire declarative close/reopen controls.
  initializeLayout(): void {
    const hud = document.getElementById('hud-container');
    if (hud) this.uiContainer = hud;

    // Wire any [data-close] buttons to hide their target panel.
    document.querySelectorAll('[data-close]').forEach((btn) => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const targetId = (btn as HTMLElement).getAttribute('data-close');
        if (targetId) document.getElementById(targetId)?.classList.add('hidden');
      });
    });

    // Floating anchor re-opens every panel (recovery from a stuck/hidden UI).
    document.getElementById('menu-launcher')?.addEventListener('click', () => {
      document.querySelectorAll('.panel').forEach((p) => p.classList.remove('hidden'));
    });

    console.log('WindowManager layout initialized');
  }

  // Create a new window
  createWindow(config: WindowConfig): string {
    const id = config.id || this.generateWindowId();
    const windowConfig = { ...config, id };

    // Check if window already exists
    if (this.windows.has(id)) {
      return id;
    }

    // Create state
    const state: WindowState = {
      isOpen: false,
      isVisible: false,
      isMinimized: false,
      isMaximized: false,
      position: config.position || { x: 100, y: 100 },
      size: {
        width: config.width || 400,
        height: config.height || 300
      },
      zIndex: 0
    };

    // Create element
    const element = document.createElement('div');
    element.id = `window-${id}`;
    element.style.cssText = `
      position: absolute;
      background: rgba(20,20,35,0.95);
      border: 1px solid rgba(100,100,150,0.3);
      border-radius: 12px;
      box-shadow: 0 8px 32px rgba(0,0,0,0.5);
      backdrop-filter: blur(10px);
      display: none;
      overflow: hidden;
    `;

    // Create content area
    const content = document.createElement('div');
    content.id = `window-content-${id}`;
    content.style.cssText = `
      padding: 16px;
      overflow: auto;
      color: rgba(255,255,255,0.9);
      font-family: 'Segoe UI', system-ui, sans-serif;
      font-size: 13px;
    `;

    // Create title bar for draggable windows
    const titleBar = config.draggable ? this.createTitleBar(id, config.title || 'Window') : null;

    // Create control buttons
    const controls = this.createControls(id, config);

    // Assemble window
    if (titleBar) {
      element.appendChild(titleBar);
    }
    element.appendChild(content);

    // Set initial position and size
    this.applyWindowState(element, state, config);

    // Store window
    this.windows.set(id, {
      config: windowConfig,
      state,
      element,
      content,
      controls,
      handlers: {
        onClose: null,
        onMinimize: null,
        onMaximize: null,
        onReset: null,
        onBringToFront: null
      }
    });

    // Set up event handlers for controls
    this.setupControlHandlers(id);

    console.log(`Window created: ${id}`);
    return id;
  }

  private createTitleBar(id: string, title: string): HTMLElement {
    const titleBar = document.createElement('div');
    titleBar.id = `window-titlebar-${id}`;
    titleBar.style.cssText = `
      height: 40px;
      background: rgba(30,30,50,0.9);
      border-bottom: 1px solid rgba(100,100,150,0.2);
      display: flex;
      align-items: center;
      padding: 0 12px;
      cursor: grab;
      user-select: none;
      flex: 0 0 auto;
    `;

    const titleText = document.createElement('span');
    titleText.style.cssText = `
      flex: 1;
      font-size: 13px;
      font-weight: 500;
      color: rgba(255,255,255,0.85);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    `;
    titleText.textContent = title;
    titleBar.appendChild(titleText);

    return titleBar;
  }

  private createControls(id: string, config: WindowConfig): {
    titleBar: HTMLElement | null;
    closeButton: HTMLElement | null;
    minimizeButton: HTMLElement | null;
    maximizeButton: HTMLElement | null;
    resetButton: HTMLElement | null;
  } {
    const controls = {
      titleBar: null,
      closeButton: null,
      minimizeButton: null,
      maximizeButton: null,
      resetButton: null
    };

    // Find title bar
    const element = document.getElementById(`window-${id}`);
    if (element) {
      controls.titleBar = element.querySelector('[id*="window-titlebar"]') as HTMLElement;
    }

    if (config.closable) {
      const closeBtn = document.createElement('button');
      closeBtn.id = `window-close-${id}`;
      closeBtn.innerHTML = '✕';
      closeBtn.style.cssText = `
        width: 24px;
        height: 24px;
        background: transparent;
        border: none;
        border-radius: 4px;
        color: rgba(255,255,255,0.5);
        cursor: pointer;
        font-size: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.15s ease;
        flex: 0 0 auto;
      `;
      closeBtn.addEventListener('mouseenter', () => {
        closeBtn.style.background = 'rgba(255,80,80,0.2)';
        closeBtn.style.color = '#ff5050';
      });
      closeBtn.addEventListener('mouseleave', () => {
        closeBtn.style.background = 'transparent';
        closeBtn.style.color = 'rgba(255,255,255,0.5)';
      });
      closeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        if (this.closeWindow(id)) {
          this.triggerCloseHandler(id);
        }
      });

      // Also handle keyboard close
      closeBtn.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          if (this.closeWindow(id)) {
            this.triggerCloseHandler(id);
          }
        }
      });

      // Store reference
      if (controls.titleBar) {
        controls.titleBar.appendChild(closeBtn);
      } else if (element) {
        element.appendChild(closeBtn);
      }
      controls.closeButton = closeBtn;
    }

    if (config.minimizable) {
      const minBtn = document.createElement('button');
      minBtn.id = `window-minimize-${id}`;
      minBtn.innerHTML = '─';
      minBtn.style.cssText = `
        width: 24px;
        height: 24px;
        background: transparent;
        border: none;
        border-radius: 4px;
        color: rgba(255,255,255,0.5);
        cursor: pointer;
        font-size: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.15s ease;
        flex: 0 0 auto;
      `;
      minBtn.addEventListener('mouseenter', () => {
        minBtn.style.background = 'rgba(255,255,255,0.1)';
        minBtn.style.color = 'rgba(255,255,255,0.8)';
      });
      minBtn.addEventListener('mouseleave', () => {
        minBtn.style.background = 'transparent';
        minBtn.style.color = 'rgba(255,255,255,0.5)';
      });
      minBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        this.minimizeWindow(id);
      });

      if (controls.titleBar) {
        controls.titleBar.appendChild(minBtn);
      } else if (element) {
        element.appendChild(minBtn);
      }
      controls.minimizeButton = minBtn;
    }

    if (config.maximizable) {
      const maxBtn = document.createElement('button');
      maxBtn.id = `window-maximize-${id}`;
      maxBtn.innerHTML = '□';
      maxBtn.style.cssText = `
        width: 24px;
        height: 24px;
        background: transparent;
        border: none;
        border-radius: 4px;
        color: rgba(255,255,255,0.5);
        cursor: pointer;
        font-size: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.15s ease;
        flex: 0 0 auto;
      `;
      maxBtn.addEventListener('mouseenter', () => {
        maxBtn.style.background = 'rgba(255,255,255,0.1)';
        maxBtn.style.color = 'rgba(255,255,255,0.8)';
      });
      maxBtn.addEventListener('mouseleave', () => {
        maxBtn.style.background = 'transparent';
        maxBtn.style.color = 'rgba(255,255,255,0.5)';
      });
      maxBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        this.toggleMaximizeWindow(id);
      });

      if (controls.titleBar) {
        controls.titleBar.appendChild(maxBtn);
      } else if (element) {
        element.appendChild(maxBtn);
      }
      controls.maximizeButton = maxBtn;
    }

    return controls;
  }

  private setupControlHandlers(id: string): void {
    const windowData = this.windows.get(id);
    if (!windowData) return;

    // Set up title bar dragging
    if (windowData.config.draggable && windowData.controls.titleBar) {
      let isDragging = false;
      let startX = 0;
      let startY = 0;
      let initialPos = { x: 0, y: 0 };

      windowData.controls.titleBar.addEventListener('mousedown', (e) => {
        if (windowData.state.isMaximized) return;
        
        isDragging = true;
        startX = e.clientX;
        startY = e.clientY;
        initialPos = { ...windowData.state.position };
        
        windowData.controls.titleBar!.style.cursor = 'grabbing';
        e.preventDefault();
      });

      document.addEventListener('mousemove', (e) => {
        if (!isDragging) return;
        
        const dx = e.clientX - startX;
        const dy = e.clientY - startY;
        
        windowData.state.position.x = Math.max(0, initialPos.x + dx);
        windowData.state.position.y = Math.max(0, initialPos.y + dy);
        
        this.applyWindowPosition(windowData.element!, windowData.state.position);
      });

      document.addEventListener('mouseup', () => {
        if (isDragging) {
          isDragging = false;
          if (windowData.controls.titleBar) {
            windowData.controls.titleBar.style.cursor = 'grab';
          }
        }
      });
    }

    // Set up resize handles if resizable
    if (windowData.config.resizable) {
      this.setupResizeHandles(id);
    }
  }

  private setupResizeHandles(id: string): void {
    const windowData = this.windows.get(id);
    if (!windowData || !windowData.element) return;

    const element = windowData.element;
    
    // Create resize handle at bottom-right
    const resizeHandle = document.createElement('div');
    resizeHandle.style.cssText = `
      position: absolute;
      bottom: 0;
      right: 0;
      width: 12px;
      height: 12px;
      cursor: nwse-resize;
      z-index: 10;
    `;
    
    let isResizing = false;
    let startX = 0;
    let startY = 0;
    let startWidth = 0;
    let startHeight = 0;

    resizeHandle.addEventListener('mousedown', (e) => {
      isResizing = true;
      startX = e.clientX;
      startY = e.clientY;
      startWidth = windowData.state.size.width;
      startHeight = windowData.state.size.height;
      e.preventDefault();
      e.stopPropagation();
    });

    document.addEventListener('mousemove', (e) => {
      if (!isResizing) return;
      
      const newWidth = Math.max(windowData.config.minWidth || 200, startWidth + (e.clientX - startX));
      const newHeight = Math.max(windowData.config.minHeight || 150, startHeight + (e.clientY - startY));
      
      windowData.state.size.width = newWidth;
      windowData.state.size.height = newHeight;
      
      this.applyWindowSize(element, windowData.state.size);
    });

    document.addEventListener('mouseup', () => {
      isResizing = false;
    });

    element.appendChild(resizeHandle);
  }

  private generateWindowId(): string {
    return `window-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  // Open a window
  openWindow(id: string, showContent: boolean = true): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) {
      console.warn(`Window not found: ${id}`);
      return false;
    }

    // Ensure window element exists in DOM
    if (windowData.element && !windowData.element.parentNode) {
      if (this.uiContainer) {
        this.uiContainer.appendChild(windowData.element);
      }
    }

    // Apply state
    windowData.state.isOpen = true;
    windowData.state.isVisible = true;
    windowData.state.isMinimized = false;
    windowData.state.isMaximized = false;

    // Set z-index
    windowData.state.zIndex = this.nextZIndex++;
    this.applyWindowZIndex(windowData.element!, windowData.state.zIndex);

    // Show content
    if (windowData.content) {
      windowData.content.style.display = showContent ? 'block' : 'none';
    }

    // Apply visibility
    this.applyWindowVisibility(windowData.element!, true);

    // Bring to front
    this.bringToFront(id);

    // Set as active window
    this.setActiveWindow(id);

    // Ensure element is displayed
    this.applyWindowDisplay(windowData.element!, true);

    // Trigger open event
    if (windowData.handlers.onBringToFront) {
      windowData.handlers.onBringToFront();
    }

    if (this.eventListeners.onWindowOpen) {
      this.eventListeners.onWindowOpen(id);
    }

    console.log(`Window opened: ${id}`);
    return true;
  }

  // Close a window
  closeWindow(id: string): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) {
      return false;
    }

    // Check if we should close
    if (!this.canCloseWindow(id)) {
      return false;
    }

    // Clear any input capture
    this.releaseInputCapture(id);

    // Remove element from DOM if not persistent
    if (!windowData.config.persistent && windowData.element && windowData.element.parentNode) {
      windowData.element.parentNode.removeChild(windowData.element);
    } else if (windowData.element) {
      this.applyWindowVisibility(windowData.element, false);
      this.applyWindowDisplay(windowData.element, false);
    }

    // Update state
    windowData.state.isOpen = false;
    windowData.state.isVisible = false;

    // Clear active window if this was active
    if (this.activeWindowId === id) {
      this.activeWindowId = null;
      if (this.eventListeners.onActiveWindowChange) {
        this.eventListeners.onActiveWindowChange(null);
      }
    }

    // Trigger close handler
    this.triggerCloseHandler(id);

    if (this.eventListeners.onWindowClose) {
      this.eventListeners.onWindowClose(id);
    }

    console.log(`Window closed: ${id}`);
    return true;
  }

  private canCloseWindow(id: string): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return true;

    // Modal windows require explicit confirmation
    if (windowData.config.modal) {
      // Modal behavior can be customized
      return true;
    }

    return true;
  }

  private releaseInputCapture(id: string): void {
    const windowData = this.windows.get(id);
    if (!windowData || !windowData.element) return;

    // Remove any pointer capture
    if (windowData.element.hasPointerCapture) {
      try {
        windowData.element.releasePointerCapture(0);
      } catch (e) {
        // Ignore capture errors
      }
    }

    // Remove any focus
    if (document.activeElement === windowData.element) {
      (document.activeElement as HTMLElement)?.blur();
    }
  }

  // Toggle a window
  toggleWindow(id: string): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return false;

    if (windowData.state.isOpen) {
      return this.closeWindow(id);
    } else {
      return this.openWindow(id);
    }
  }

  // Minimize a window
  minimizeWindow(id: string): boolean {
    const windowData = this.windows.get(id);
    if (!windowData || !windowData.state.isOpen) return false;

    windowData.state.isMinimized = true;
    
    // Hide content, keep title bar
    if (windowData.content) {
      windowData.content.style.display = 'none';
    }

    console.log(`Window minimized: ${id}`);
    return true;
  }

  // Maximize a window
  maximizeWindow(id: string): boolean {
    const windowData = this.windows.get(id);
    if (!windowData || !windowData.state.isOpen) return false;

    windowData.state.isMaximized = true;
    windowData.state.position = { x: 0, y: 0 };
    windowData.state.size = {
      width: this.uiContainer?.clientWidth || window.innerWidth,
      height: this.uiContainer?.clientHeight || window.innerHeight
    };

    this.applyWindowPosition(windowData.element!, windowData.state.position);
    this.applyWindowSize(windowData.element!, windowData.state.size);
    this.applyWindowBorder(windowData.element!, true);

    console.log(`Window maximized: ${id}`);
    return true;
  }

  // Toggle maximize
  toggleMaximizeWindow(id: string): boolean {
    const windowData = this.windows.get(id);
    if (!windowData || !windowData.state.isOpen) return false;

    if (windowData.state.isMaximized) {
      // Restore from maximize
      windowData.state.isMaximized = false;
      // Could restore previous size/position here
      this.applyWindowBorder(windowData.element!, false);
    } else {
      this.maximizeWindow(id);
    }

    return true;
  }

  // Bring window to front
  bringToFront(id: string): boolean {
    const windowData = this.windows.get(id);
    if (!windowData || !windowData.state.isOpen) return false;

    windowData.state.zIndex = this.nextZIndex++;
    this.applyWindowZIndex(windowData.element!, windowData.state.zIndex);

    this.setActiveWindow(id);

    return true;
  }

  // Send window to back
  sendToBack(id: string): boolean {
    const windowData = this.windows.get(id);
    if (!windowData || !windowData.state.isOpen) return false;

    windowData.state.zIndex = 1;
    this.applyWindowZIndex(windowData.element!, 1);

    return true;
  }

  // Reset window to default state
  resetWindow(id: string): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return false;

    // Reset position to default
    if (windowData.config.initPosition === 'center') {
      const containerWidth = this.uiContainer?.clientWidth || window.innerWidth;
      const containerHeight = this.uiContainer?.clientHeight || window.innerHeight;
      windowData.state.position = {
        x: (containerWidth - windowData.state.size.width) / 2,
        y: (containerHeight - windowData.state.size.height) / 2
      };
    } else if (windowData.config.initPosition) {
      const { width, height } = windowData.state.size;
      const containerWidth = this.uiContainer?.clientWidth || window.innerWidth;
      const containerHeight = this.uiContainer?.clientHeight || window.innerHeight;
      
      switch (windowData.config.initPosition) {
        case 'top-left':
          windowData.state.position = { x: 10, y: 50 };
          break;
        case 'top-right':
          windowData.state.position = { x: containerWidth - width - 10, y: 50 };
          break;
        case 'bottom-left':
          windowData.state.position = { x: 10, y: containerHeight - height - 10 };
          break;
        case 'bottom-right':
          windowData.state.position = { x: containerWidth - width - 10, y: containerHeight - height - 10 };
          break;
      }
    }

    // Reset size
    windowData.state.size = {
      width: windowData.config.width || 400,
      height: windowData.config.height || 300
    };

    windowData.state.isMinimized = false;
    windowData.state.isMaximized = false;

    this.applyWindowPosition(windowData.element!, windowData.state.position);
    this.applyWindowSize(windowData.element!, windowData.state.size);
    this.applyWindowBorder(windowData.element!, false);

    if (windowData.content) {
      windowData.content.style.display = 'block';
    }

    return true;
  }

  // Set window content
  setContent(id: string, content: HTMLElement | string | null): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return false;

    if (!windowData.content) return false;

    if (typeof content === 'string') {
      windowData.content.innerHTML = content;
    } else if (content instanceof HTMLElement) {
      windowData.content.innerHTML = '';
      windowData.content.appendChild(content);
    } else {
      windowData.content.innerHTML = '';
    }

    return true;
  }

  // Get content element
  getContent(id: string): HTMLElement | null {
    const windowData = this.windows.get(id);
    return windowData?.content || null;
  }

  // Register close handler
  onClose(id: string, handler: () => void): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return false;

    windowData.handlers.onClose = handler;
    return true;
  }

  // Register minimize handler
  onMinimize(id: string, handler: () => void): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return false;

    windowData.handlers.onMinimize = handler;
    return true;
  }

  // Register maximize handler
  onMaximize(id: string, handler: () => void): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return false;

    windowData.handlers.onMaximize = handler;
    return true;
  }

  // Register reset handler
  onReset(id: string, handler: () => void): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return false;

    windowData.handlers.onReset = handler;
    return true;
  }

  // Register bring to front handler
  onBringToFront(id: string, handler: () => void): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return false;

    windowData.handlers.onBringToFront = handler;
    return true;
  }

  private triggerCloseHandler(id: string): void {
    const windowData = this.windows.get(id);
    if (windowData?.handlers.onClose) {
      windowData.handlers.onClose();
    }
  }

  // Check if window is open
  isOpen(id: string): boolean {
    return this.windows.get(id)?.state.isOpen || false;
  }

  // Check if window is visible
  isVisible(id: string): boolean {
    return this.windows.get(id)?.state.isVisible || false;
  }

  // Set window position
  setPosition(id: string, x: number, y: number): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return false;

    windowData.state.position = { x, y };
    this.applyWindowPosition(windowData.element!, windowData.state.position);
    return true;
  }

  // Set window size
  setSize(id: string, width: number, height: number): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return false;

    windowData.state.size = { width, height };
    this.applyWindowSize(windowData.element!, windowData.state.size);
    return true;
  }

  // Set window title
  setTitle(id: string, title: string): boolean {
    const windowData = this.windows.get(id);
    if (!windowData) return false;

    windowData.config.title = title;
    
    // Update title bar text
    const titleBar = windowData.controls.titleBar;
    if (titleBar) {
      const titleText = titleBar.querySelector('span') as HTMLElement;
      if (titleText) {
        titleText.textContent = title;
      }
    }

    return true;
  }

  // Check if window exists
  hasWindow(id: string): boolean {
    return this.windows.has(id);
  }

  // Get window state
  getState(id: string): WindowState | null {
    return this.windows.get(id)?.state || null;
  }

  // Get all window IDs
  getWindowIds(): string[] {
    return Array.from(this.windows.keys());
  }

  // Clear all windows
  clearAll(): void {
    const ids = this.getWindowIds();
    ids.forEach(id => this.closeWindow(id));
  }

  // Set active window
  private setActiveWindow(id: string | null): void {
    this.previousWindowId = this.activeWindowId;
    this.activeWindowId = id;

    if (this.eventListeners.onActiveWindowChange) {
      this.eventListeners.onActiveWindowChange(id);
    }

    if (this.eventListeners.onWindowFocus && id) {
      this.eventListeners.onWindowFocus(id);
    }
  }

  // Get active window
  getActiveWindow(): string | null {
    return this.activeWindowId;
  }

  // Get previous window
  getPreviousWindow(): string | null {
    return this.previousWindowId;
  }

  // ESC key handler - close active window or go back
  handleEscape(): boolean {
    if (this.activeWindowId) {
      const windowData = this.windows.get(this.activeWindowId);
      if (windowData?.config.closeOnEscape) {
        return this.closeWindow(this.activeWindowId);
      }
    }
    return false;
  }

  // Click outside handler
  handleOutsideClick(x: number, y: number): boolean {
    if (this.activeWindowId) {
      const windowData = this.windows.get(this.activeWindowId);
      if (windowData?.config.closeOnOutsideClick && !this.isClickInsideWindow(this.activeWindowId, x, y)) {
        return this.closeWindow(this.activeWindowId);
      }
    }
    return false;
  }

  private isClickInsideWindow(id: string, x: number, y: number): boolean {
    const windowData = this.windows.get(id);
    if (!windowData || !windowData.element) return false;

    const rect = windowData.element.getBoundingClientRect();
    return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom;
  }

  // Remove a window entirely
  removeWindow(id: string): boolean {
    if (!this.windows.has(id)) return false;

    this.closeWindow(id);
    this.windows.delete(id);
    return true;
  }

  // Destroy the window manager
  destroy(): void {
    this.clearAll();
    this.windows.clear();
    this.uiContainer = null;
    this.activeWindowId = null;
    this.previousWindowId = null;
  }

  // Apply window styles
  private applyWindowState(element: HTMLElement | null, state: WindowState, config: WindowConfig): void {
    if (!element) return;

    this.applyWindowPosition(element, state.position);
    this.applyWindowSize(element, state.size);
    this.applyWindowDisplay(element, state.isVisible);
    this.applyWindowZIndex(element, state.zIndex);
  }

  private applyWindowPosition(element: HTMLElement | null, position: { x: number; y: number }): void {
    if (!element) return;
    element.style.left = `${position.x}px`;
    element.style.top = `${position.y}px`;
  }

  private applyWindowSize(element: HTMLElement | null, size: { width: number; height: number }): void {
    if (!element) return;
    element.style.width = `${size.width}px`;
    element.style.height = `${size.height}px`;
  }

  private applyWindowVisibility(element: HTMLElement | null, visible: boolean): void {
    if (!element) return;
    element.style.visibility = visible ? 'visible' : 'hidden';
  }

  private applyWindowDisplay(element: HTMLElement | null, display: boolean): void {
    if (!element) return;
    element.style.display = display ? 'block' : 'none';
  }

  private applyWindowZIndex(element: HTMLElement | null, zIndex: number): void {
    if (!element) return;
    element.style.zIndex = `${zIndex}`;
  }

  private applyWindowBorder(element: HTMLElement | null, maximized: boolean): void {
    if (!element) return;
    element.style.borderRadius = maximized ? '0' : '12px';
  }
}
