import * as THREE from 'three';
import { WindowManager } from './WindowManager';
import { InputManager } from './InputManager';
import { TimeController } from './TimeController';
import { Renderer } from '../rendering/Renderer';
import { Camera } from '../rendering/Camera';
import { Universe } from '../simulation/Universe';
import { ObjectManager } from '../objects/ObjectManager';
import { ToolSystem } from '../tools/ToolSystem';
import { WidgetSystem } from '../widgets/WidgetSystem';
import { ExperimentGenerator } from '../experiments/ExperimentGenerator';
import { ChaosMode } from '../experiments/ChaosMode';

export class App {
    private canvas: HTMLCanvasElement;
    private renderer!: Renderer;
    private camera!: Camera;
    private windowManager!: WindowManager;
    private inputManager!: InputManager;
    private timeController!: TimeController;
    private universe!: Universe;
    private objectManager!: ObjectManager;
    private toolSystem!: ToolSystem;
    private widgetSystem!: WidgetSystem;
    private experimentGenerator!: ExperimentGenerator;
    private chaosMode!: ChaosMode;
    
    private isRunning: boolean = false;
    private lastTimestamp: number = 0;

    constructor() {
        this.canvas = document.getElementById('renderCanvas') as HTMLCanvasElement;
        if (!this.canvas) {
            this.canvas = document.createElement('canvas');
            this.canvas.id = 'renderCanvas';
            document.body.appendChild(this.canvas);
        }
    }

    public async init(): Promise<void> {
        console.log("Initializing Unlimited Possibilities Sandbox Engine Core...");
        
        // Initialize Critical Core Foundations (Zero Null Traps)
        this.windowManager = new WindowManager();
        this.timeController = new TimeController();
        this.renderer = new Renderer(this.canvas);
        this.camera = new Camera(this.renderer.getThreeRenderer());
        this.inputManager = new InputManager(this.canvas, this.camera);
        
        // Initialize Central Simulation & Physics Matrices
        this.universe = new Universe(this.renderer.getScene());
        this.objectManager = new ObjectManager(this.renderer.getScene(), this.universe);
        
        // Initialize App-Style Interactive Utility Toolboxes
        this.toolSystem = new ToolSystem(this.universe, this.objectManager);
        this.widgetSystem = new WidgetSystem(this.windowManager);
        this.experimentGenerator = new ExperimentGenerator(this.universe, this.objectManager);
        this.chaosMode = new ChaosMode(this.universe, this.objectManager);

        // Cross-wire subsystems cleanly to complete interface contracts
        this.inputManager.setToolSystem(this.toolSystem);
        this.windowManager.initializeLayout();
        
        // Window Manager Safe Recovery Event Binding (Fixes UI Trap and P0 Blocking Defects)
        window.addEventListener('resize', () => this.handleResize());
        console.log("Engine Core Initialization Sequence Complete.");
    }

    public start(): void {
        if (this.isRunning) return;
        this.isRunning = true;
        this.lastTimestamp = performance.now();
        requestAnimationFrame((timestamp) => this.loop(timestamp));
        console.log("Production Execution Loop Dispatched Successfully.");
    }

    private loop(timestamp: number): void {
        if (!this.isRunning) return;

        // Extract tick delta using our safe centralized time controller matrix
        const rawDelta = (timestamp - this.lastTimestamp) / 1000;
        this.lastTimestamp = timestamp;

        // Apply progressive simulation speed configurations (Pauses, Slow-Mo)
        const simulationDelta = rawDelta * this.timeController.getSpeedMultiplier();
        const totalElapsedTime = this.timeController.getElapsedTime();

        // Core Tick Cycle: Update simulation arrays, inputs, and render the frame graph
        this.inputManager.update(rawDelta);
        this.universe.update(simulationDelta, totalElapsedTime);
        this.objectManager.update(simulationDelta);
        this.toolSystem.update(rawDelta);
        
        // Frame Generation Output Matrix Stage
        this.renderer.render(this.camera.getThreeCamera());

        // Continually re-queue the cycle animation frame
        requestAnimationFrame((t) => this.loop(t));
    }

    private handleResize(): void {
        this.renderer.resize();
        this.camera.resize();
    }
}
