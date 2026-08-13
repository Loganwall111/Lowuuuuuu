// Test entry: re-exports the AI system together with the maths type the
// tests need, so the harness works with one bundle.
export * from '../../src/bjs/systems/AISystem';
export { Vector3 } from '@babylonjs/core/Maths/math.vector';
