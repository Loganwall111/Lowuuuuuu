import { Universe } from '../simulation/Universe';
import { ObjectManager } from '../objects/ObjectManager';

export class ToolSystem {
    private activeTool: string = 'select';

    constructor(private universe: Universe, private objectManager: ObjectManager) {}

    public setTool(toolId: string): void {
        this.activeTool = toolId;
        console.log(`Tool changed to: ${this.activeTool}`);
    }

    public update(delta: number): void {
        // Tool-specific logic (dragging, spawning, etc.)
    }
}
