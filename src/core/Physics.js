// AGENT 03 — NEWTON — Simple Physics & Collision (AABB + Raycast, no heavy engine for perf)
import * as THREE from 'three'

export class Physics {
  constructor(scene){
    this.scene=scene
    this.gravity = -24
    this.colliders = [] // { min:Vector3, max:Vector3, mesh? }
  }
  addBoxCollider(min, max, mesh=null){
    this.colliders.push({ min:min.clone(), max:max.clone(), mesh })
  }
  clear(){ this.colliders.length=0 }
  // Raycast against colliders + ground plane y=0
  raycast(origin, dir, maxDist=200){
    let closest = null
    let dist = maxDist
    // ground
    if(dir.y !== 0){
      const t = -origin.y / dir.y
      if(t>0 && t<dist){
        dist=t
        closest={ point: origin.clone().add(dir.clone().multiplyScalar(t)), normal: new THREE.Vector3(0,1,0), distance:t, collider:null }
      }
    }
    for(const c of this.colliders){
      const hit = this._rayAABB(origin, dir, c.min, c.max)
      if(hit && hit.distance < dist){
        dist = hit.distance
        closest = { ...hit, collider:c }
      }
    }
    return closest
  }
  _rayAABB(o,d, min,max){
    let tmin = -Infinity, tmax = Infinity
    for(let i=0;i<3;i++){
      const oi=o.getComponent(i), di=d.getComponent(i)
      const mn=min.getComponent(i), mx=max.getComponent(i)
      if(Math.abs(di)<1e-6){
        if(oi < mn || oi > mx) return null
      } else {
        let t1=(mn-oi)/di, t2=(mx-oi)/di
        if(t1>t2) [t1,t2]=[t2,t1]
        tmin=Math.max(tmin,t1); tmax=Math.min(tmax,t2)
        if(tmin>tmax) return null
      }
    }
    if(tmin<0) return null
    const point = o.clone().add(d.clone().multiplyScalar(tmin))
    // normal estimate
    const eps=1e-3
    let normal=new THREE.Vector3()
    if(Math.abs(point.x-min.x)<eps) normal.set(-1,0,0)
    else if(Math.abs(point.x-max.x)<eps) normal.set(1,0,0)
    else if(Math.abs(point.y-min.y)<eps) normal.set(0,-1,0)
    else if(Math.abs(point.y-max.y)<eps) normal.set(0,1,0)
    else if(Math.abs(point.z-min.z)<eps) normal.set(0,0,-1)
    else if(Math.abs(point.z-max.z)<eps) normal.set(0,0,1)
    return { point, normal, distance:tmin }
  }
  checkAABB(pos, halfSize, excludeMesh=null){
    const min = pos.clone().sub(halfSize)
    const max = pos.clone().add(halfSize)
    for(const c of this.colliders){
      if(excludeMesh && c.mesh===excludeMesh) continue
      if(min.x < c.max.x && max.x > c.min.x &&
         min.y < c.max.y && max.y > c.min.y &&
         min.z < c.max.z && max.z > c.min.z){
        // penetration vector
        const pen = new THREE.Vector3(
          Math.min(max.x - c.min.x, c.max.x - min.x),
          Math.min(max.y - c.min.y, c.max.y - min.y),
          Math.min(max.z - c.min.z, c.max.z - min.z)
        )
        return { hit:true, pen, collider:c }
      }
    }
    return { hit:false }
  }
  checkAABBExcluding(pos, halfSize, excludeSet){
    const min = pos.clone().sub(halfSize)
    const max = pos.clone().add(halfSize)
    for(const c of this.colliders){
      if(excludeSet && excludeSet.has(c.mesh)) continue
      if(min.x < c.max.x && max.x > c.min.x &&
         min.y < c.max.y && max.y > c.min.y &&
         min.z < c.max.z && max.z > c.min.z){
        const pen = new THREE.Vector3(
          Math.min(max.x - c.min.x, c.max.x - min.x),
          Math.min(max.y - c.min.y, c.max.y - min.y),
          Math.min(max.z - c.min.z, c.max.z - min.z)
        )
        return { hit:true, pen, collider:c }
      }
    }
    return { hit:false }
  }
}
