import { WindowManager } from '../core/WindowManager';

export class WidgetSystem {
    constructor(private windowManager: WindowManager) {}

    public initializeWidgets(): void {
        console.log("Widget system initialized with futuristic HUD elements.");
    }
}
