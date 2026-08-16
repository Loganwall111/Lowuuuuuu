/** Physical holographic apertures for PortalGunSystem's pure travel model. */
import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';
import type { Portal } from './PortalGunSystem';

interface Aperture { key: string; root: Mesh; rings: Mesh[]; mats: StandardMaterial[]; }

export class PortalVisualSystem {
  private scene: Scene | null = null;
  private live = new Map<string, Aperture>();
  private target = new Vector3();
  private t = 0;

  attach(scene: Scene): void { this.dispose(); this.scene = scene; }

  update(dt: number, portals: Array<Portal | null>, worldId: string): void {
    if (!this.scene) return;
    this.t += Math.max(0, Math.min(.1, dt));
    const wanted = new Set<string>();
    for (const p of portals) {
      if (!p?.open || p.worldId !== worldId) continue;
      const key = p.slot + ':' + p.worldId;
      wanted.add(key);
      let a = this.live.get(key);
      if (!a) { a = this.build(p); this.live.set(key, a); }
      this.place(a, p);
      for (let i = 0; i < a.rings.length; i++) {
        a.rings[i].rotation.z += dt * (i % 2 ? -1.4 : .95) * (i + 1);
        const pulse = 1 + Math.sin(this.t * 3.2 + i) * .025;
        a.rings[i].scaling.setAll(pulse);
        a.mats[i].alpha = .48 + .16 * Math.sin(this.t * 2.4 + i);
      }
    }
    for (const [key, a] of this.live) {
      if (wanted.has(key)) continue;
      this.destroy(a); this.live.delete(key);
    }
  }

  private build(p: Portal): Aperture {
    const scene = this.scene!;
    const root = MeshBuilder.CreateDisc('portalVeil_' + p.slot,
      { radius: p.radius * .93, tessellation: 64 }, scene);
    const core = new StandardMaterial('portalVeilM_' + p.slot, scene);
    core.diffuseColor = Color3.Black(); core.specularColor = Color3.Black();
    core.emissiveColor = p.slot === 'a' ? new Color3(0, .2, .32) : new Color3(.2, .04, .34);
    core.alpha = .5; core.alphaMode = 1; core.disableLighting = true;
    core.disableDepthWrite = true; core.backFaceCulling = false;
    root.material = core; root.isPickable = false; root.renderingGroupId = 1;
    const rings: Mesh[] = [], mats: StandardMaterial[] = [];
    for (let i = 0; i < 3; i++) {
      const ring = MeshBuilder.CreateTorus('portalArc_' + p.slot + i, {
        diameter: p.radius * 2 * (1 + i * .075), thickness: .08 + i * .025,
        tessellation: 72
      }, scene);
      const m = new StandardMaterial('portalArcM_' + p.slot + i, scene);
      m.diffuseColor = Color3.Black(); m.specularColor = Color3.Black();
      m.emissiveColor = p.slot === 'a'
        ? new Color3(.02 + i * .04, .65 + i * .1, 1)
        : new Color3(.5 + i * .12, .12, 1);
      m.alpha = .6; m.alphaMode = 1; m.disableLighting = true;
      m.disableDepthWrite = true; m.backFaceCulling = false;
      ring.material = m; ring.isPickable = false; ring.renderingGroupId = 1;
      rings.push(ring); mats.push(m);
    }
    mats.push(core);
    return { key: p.slot, root, rings, mats };
  }

  private place(a: Aperture, p: Portal): void {
    // Babylon discs face +Z. lookAt establishes the portal normal.
    p.position.addToRef(p.normal, this.target);
    a.root.position.copyFrom(p.position); a.root.lookAt(this.target);
    for (const r of a.rings) { r.position.copyFrom(p.position); r.lookAt(this.target); }
  }

  private destroy(a: Aperture): void {
    try { a.root.dispose(); } catch { /* gone */ }
    for (const r of a.rings) try { r.dispose(); } catch { /* gone */ }
    for (const m of a.mats) try { m.dispose(); } catch { /* gone */ }
  }

  dispose(): void {
    for (const a of this.live.values()) this.destroy(a);
    this.live.clear(); this.scene = null;
  }
}
