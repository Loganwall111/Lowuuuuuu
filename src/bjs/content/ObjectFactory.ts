/**
 * ObjectFactory — turns an ObjectDef into a real mesh.
 *
 * Composite shapes (duck, rocket, UFO, creature…) are built by merging
 * primitives, so the whole catalogue works without a single downloaded asset
 * and nothing can 404 into a black material.
 */

import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { PBRMaterial } from '@babylonjs/core/Materials/PBR/pbrMaterial';
import type { Scene } from '@babylonjs/core/scene';
import { type ObjectDef, MATERIALS, Material } from './ObjectCatalog';

function mat(scene: Scene, def: ObjectDef, emissive = 0): PBRMaterial {
  const m = new PBRMaterial('m_' + def.id + '_' + Math.random().toString(36).slice(2, 7), scene);
  const col = def.color ?? MATERIALS[def.material].color;
  m.albedoColor = new Color3(col[0], col[1], col[2]);

  switch (def.material) {
    case Material.Metal:  m.metallic = 0.92; m.roughness = 0.28; break;
    case Material.Glass:  m.metallic = 0.1;  m.roughness = 0.08; m.alpha = 0.55; break;
    case Material.Ice:    m.metallic = 0.05; m.roughness = 0.14; m.alpha = 0.82; break;
    case Material.Rubber: m.metallic = 0.0;  m.roughness = 0.85; break;
    case Material.Gel:    m.metallic = 0.0;  m.roughness = 0.25; m.alpha = 0.75; break;
    case Material.Plasma: m.metallic = 0.0;  m.roughness = 1.0;  break;
    case Material.Alien:  m.metallic = 0.55; m.roughness = 0.3;  break;
    default:              m.metallic = 0.05; m.roughness = 0.8;
  }

  const glow = def.material === Material.Plasma ? 1.6
    : def.material === Material.Alien ? 0.35
    : def.material === Material.Explosive ? 0.5
    : emissive;
  if (glow > 0) m.emissiveColor = new Color3(col[0] * glow, col[1] * glow, col[2] * glow);

  return m;
}

/** Builds an unparented mesh of roughly `def.radius` in size, centred on origin. */
export function buildObjectMesh(scene: Scene, def: ObjectDef): Mesh {
  const r = def.radius;
  const parts: Mesh[] = [];
  const name = def.id + '_' + Math.random().toString(36).slice(2, 7);

  const sphere = (d: number, seg = 24) => MeshBuilder.CreateSphere(name, { diameter: d, segments: seg }, scene);
  const box = (w: number, h: number, dp: number) => MeshBuilder.CreateBox(name, { width: w, height: h, depth: dp }, scene);

  switch (def.shape) {
    case 'sphere':
      parts.push(sphere(r * 2, 32));
      break;

    case 'box':
      parts.push(box(r * 1.8, r * 1.4, r * 1.2));
      break;

    case 'capsule':
      parts.push(MeshBuilder.CreateCapsule(name, { radius: r * 0.45, height: r * 2.2, tessellation: 20 }, scene));
      break;

    case 'cylinder':
      parts.push(MeshBuilder.CreateCylinder(name, { diameter: r * 1.8, height: r * 0.7, tessellation: 28 }, scene));
      break;

    case 'cone':
      parts.push(MeshBuilder.CreateCylinder(name, { diameterTop: 0, diameterBottom: r * 1.7, height: r * 2, tessellation: 26 }, scene));
      break;

    case 'torus':
      parts.push(MeshBuilder.CreateTorus(name, { diameter: r * 1.9, thickness: r * 0.55, tessellation: 30 }, scene));
      break;

    case 'ring':
      parts.push(MeshBuilder.CreateTorus(name, { diameter: r * 2, thickness: r * 0.18, tessellation: 44 }, scene));
      break;

    case 'blob': {
      // irregular lump: a sphere with randomised vertices
      const m = sphere(r * 2, 20);
      const pos = m.getVerticesData('position');
      if (pos) {
        for (let i = 0; i < pos.length; i += 3) {
          const f = 0.78 + Math.random() * 0.42;
          pos[i] *= f; pos[i + 1] *= f; pos[i + 2] *= f;
        }
        m.updateVerticesData('position', pos);
        m.createNormals(true);
      }
      parts.push(m);
      break;
    }

    case 'star': {
      const core = sphere(r * 2, 32);
      parts.push(core);
      break;
    }

    case 'duck': {
      const body = sphere(r * 1.5, 22);
      const head = sphere(r * 0.85, 18);
      head.position.set(r * 0.55, r * 0.72, 0);
      const beak = MeshBuilder.CreateCylinder(name, { diameterTop: 0, diameterBottom: r * 0.3, height: r * 0.45, tessellation: 12 }, scene);
      beak.rotation.z = -Math.PI / 2;
      beak.position.set(r * 1.05, r * 0.66, 0);
      parts.push(body, head, beak);
      break;
    }

    case 'rocket': {
      const body = MeshBuilder.CreateCylinder(name, { diameter: r * 0.6, height: r * 1.8, tessellation: 20 }, scene);
      const nose = MeshBuilder.CreateCylinder(name, { diameterTop: 0, diameterBottom: r * 0.6, height: r * 0.7, tessellation: 20 }, scene);
      nose.position.y = r * 1.25;
      parts.push(body, nose);
      for (let i = 0; i < 3; i++) {
        const fin = box(r * 0.1, r * 0.5, r * 0.45);
        const a = (i / 3) * Math.PI * 2;
        fin.position.set(Math.cos(a) * r * 0.34, -r * 0.75, Math.sin(a) * r * 0.34);
        fin.rotation.y = -a;
        parts.push(fin);
      }
      break;
    }

    case 'ufo': {
      const disc = MeshBuilder.CreateCylinder(name, { diameterTop: r * 0.9, diameterBottom: r * 2, height: r * 0.34, tessellation: 34 }, scene);
      const dome = sphere(r * 0.9, 20);
      dome.position.y = r * 0.3;
      dome.scaling.y = 0.6;
      parts.push(disc, dome);
      break;
    }

    case 'creature': {
      const body = sphere(r * 1.3, 20);
      body.scaling.y = 1.25;
      const head = sphere(r * 0.7, 16);
      head.position.y = r * 0.95;
      parts.push(body, head);
      for (let i = 0; i < 2; i++) {
        const leg = MeshBuilder.CreateCapsule(name, { radius: r * 0.13, height: r * 0.75 }, scene);
        leg.position.set((i ? 1 : -1) * r * 0.34, -r * 0.85, 0);
        parts.push(leg);
      }
      break;
    }

    default:
      parts.push(sphere(r * 2, 24));
  }

  let mesh: Mesh;
  if (parts.length === 1) {
    mesh = parts[0];
  } else {
    const merged = Mesh.MergeMeshes(parts, true, true, undefined, false, false);
    mesh = merged ?? parts[0];
  }
  mesh.name = name;
  mesh.material = mat(scene, def);
  return mesh;
}
