import { Universe } from '../simulation/Universe';
import { ObjectManager } from '../objects/ObjectManager';

export class ChaosMode {
    private isActive: boolean = false;

    constructor(private universe: Universe, private objectManager: ObjectManager) {}

    public toggle(): void {
        this.isActive = !this.isActive;
        console.log(`Chaos Mode: ${this.isActive ? 'ACTIVE' : 'INACTIVE'}`);
    }

    public update(delta: number): void {
        if (!this.isActive) return;
        // High-intensity random physics force application
    }
}
