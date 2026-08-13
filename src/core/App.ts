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
import { SceneBuilder, WorldHandles } from '../scenes/SceneBuilder';
import { MainMenu, WorldId } from '../ui/MainMenu';

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

    // Front end + world state
    private sceneBuilder!: SceneBuilder;
    private menu!: MainMenu;
    private activeWorld: WorldHandles | null = null;
    private activeWorldName: string = '—';
    private elapsed: number = 0;
    private fps: number = 60;

    // Orbit camera state
    private orbitRadius: number = 34;
    private orbitTheta: number = -Math.PI / 2;
    private orbitPhi: number = 1.14;

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

        // ---- Front end + visible content ----
        const scene = this.renderer.getScene();
        this.sceneBuilder = new SceneBuilder();
        scene.add(this.sceneBuilder.buildStarfield());

        this.menu = new MainMenu();
        this.menu.onWorldSelected((id) => this.loadWorld(id));

        this.attachOrbitControls();
        console.log("Engine Core Initialization Sequence Complete.");
    }

    /** Swap the active world, disposing the previous one. */
    private loadWorld(id: WorldId): void {
        const scene = this.renderer.getScene();

        if (this.activeWorld) {
            scene.remove(this.activeWorld.group);
            this.activeWorld.group.traverse((o: any) => {
                if (o.geometry) o.geometry.dispose();
                if (o.material) {
                    const mats = Array.isArray(o.material) ? o.material : [o.material];
                    mats.forEach((m: any) => { if (m.map) m.map.dispose(); m.dispose(); });
                }
            });
            this.activeWorld = null;
        }

        const built =
            id === 'planetary' ? this.sceneBuilder.buildPlanetary()
          : id === 'stellar'   ? this.sceneBuilder.buildStellar()
          :                      this.sceneBuilder.buildFluid();

        scene.add(built.group);
        this.activeWorld = built;
        this.activeWorldName = id;

        // Frame the world sensibly
        const dist = id === 'planetary' ? 34 : id === 'stellar' ? 40 : 46;
        this.orbitRadius = dist;
        this.orbitPhi = id === 'fluid' ? 1.16 : 1.14;
        this.applyOrbit();

        console.log(`World loaded: ${id} (${built.bodies} bodies)`);
    }

    /** Drag to orbit, wheel to zoom — applied straight to the rescued Camera. */
    private attachOrbitControls(): void {
        const el = this.canvas;
        let dragging = false;
        let lx = 0, ly = 0;

        el.addEventListener('pointerdown', (e) => {
            dragging = true; lx = e.clientX; ly = e.clientY;
            el.setPointerCapture(e.pointerId);
        });
        el.addEventListener('pointerup', (e) => {
            dragging = false;
            try { el.releasePointerCapture(e.pointerId); } catch { /* noop */ }
        });
        el.addEventListener('pointermove', (e) => {
            if (!dragging) return;
            this.orbitTheta -= (e.clientX - lx) * 0.005;
            this.orbitPhi = Math.max(0.12, Math.min(Math.PI - 0.12, this.orbitPhi - (e.clientY - ly) * 0.005));
            lx = e.clientX; ly = e.clientY;
            this.applyOrbit();
        });
        el.addEventListener('wheel', (e) => {
            e.preventDefault();
            this.orbitRadius = Math.max(8, Math.min(220, this.orbitRadius + e.deltaY * 0.05));
            this.applyOrbit();
        }, { passive: false });
    }

    private applyOrbit(): void {
        const cam = this.camera.getThreeCamera();
        const r = this.orbitRadius, p = this.orbitPhi, t = this.orbitTheta;
        cam.position.set(
            r * Math.sin(p) * Math.cos(t),
            r * Math.cos(p),
            r * Math.sin(p) * Math.sin(t)
        );
        cam.lookAt(0, 0, 0);
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

        // Advance the active world and refresh the HUD readout
        this.elapsed += simulationDelta;
        if (this.activeWorld) this.activeWorld.tick(this.elapsed, simulationDelta);
        if (rawDelta > 0) this.fps += (1 / rawDelta - this.fps) * 0.08;
        this.menu.setStats(this.fps, this.activeWorld ? this.activeWorld.bodies : 0, this.activeWorldName);
        
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
