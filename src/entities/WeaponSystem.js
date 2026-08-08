// AGENT 14 — ARSENAL — Weapons, Ballistics, Raycast Combat
import * as THREE from 'three'

export class WeaponSystem {
  constructor(scene, physics, audio, police){
    this.scene=scene; this.physics=physics; this.audio=audio; this.police=police
    this.bullets=[]
    this.impacts=[]
    this.muzzleGroup=new THREE.Group()
    this.scene.add(this.muzzleGroup)
  }
  shoot(origin, dir){
    this.audio?.shot()
    // trail
    const end=origin.clone().add(dir.clone().multiplyScalar(180))
    const hit=this.physics.raycast(origin, dir, 190)
    const hitPoint = hit? hit.point : end
    const dist = hit? hit.distance : 190

    // tracer line
    const geo=new THREE.BufferGeometry().setFromPoints([origin, hitPoint])
    const mat=new THREE.LineBasicMaterial({ color:0xFFE066, transparent:true, opacity:0.95 })
    const line=new THREE.Line(geo, mat)
    this.scene.add(line)
    setTimeout(()=>this.scene.remove(line), 55)

    // muzzle flash
    const flash=new THREE.PointLight(0xFFE066, 420, 18)
    flash.position.copy(origin)
    this.scene.add(flash)
    const flashMesh=new THREE.Mesh(new THREE.SphereGeometry(0.28,6,6), new THREE.MeshBasicMaterial({color:0xFFF4A6, transparent:true, opacity:0.9}))
    flashMesh.position.copy(origin)
    this.scene.add(flashMesh)
    setTimeout(()=>{ this.scene.remove(flash); this.scene.remove(flashMesh)}, 70)

    if(hit){
      // impact effect
      const impact=new THREE.Mesh(new THREE.SphereGeometry(0.18,6,6), new THREE.MeshBasicMaterial({color:0xFF2E8A}))
      impact.position.copy(hitPoint)
      this.scene.add(impact)
      setTimeout(()=>this.scene.remove(impact), 180)
      // decal
      const decal=new THREE.Mesh(new THREE.CircleGeometry(0.22,8), new THREE.MeshBasicMaterial({color:0x111827, transparent:true, opacity:0.9}))
      decal.position.copy(hitPoint.clone().add(hit.normal.clone().multiplyScalar(0.02)))
      decal.lookAt(hitPoint.clone().add(hit.normal))
      this.scene.add(decal)
      setTimeout(()=>this.scene.remove(decal), 5000)
      // spark
      for(let i=0;i<4;i++){
        const p=new THREE.Mesh(new THREE.SphereGeometry(0.06,4,4), new THREE.MeshBasicMaterial({color:0xFFD600}))
        p.position.copy(hitPoint)
        p.userData={ vel: new THREE.Vector3((Math.random()-0.5)*6, Math.random()*5, (Math.random()-0.5)*6) }
        this.scene.add(p)
        this.impacts.push({ mesh:p, t:0 })
      }
      // if hit is cop? handled elsewhere; increase wanted slightly
      if(Math.random()>0.4) this.police?.addWanted(0) // keep heat
      else if(Math.random()>0.85) this.police?.addWanted(1)
    } else {
      // provoke police if shooting in air near cops
      if(Math.random()>0.7) this.police?.addWanted(0.2)
    }

    // kick wanted for shooting
    this.police?.addWanted(0.15)
  }
  update(dt){
    for(let i=this.impacts.length-1;i>=0;i--){
      const it=this.impacts[i]
      it.t+=dt
      it.mesh.position.add(it.mesh.userData.vel.clone().multiplyScalar(dt))
      it.mesh.userData.vel.y -= 12*dt
      it.mesh.material.opacity = 1 - it.t*3
      if(it.t>0.4){ this.scene.remove(it.mesh); this.impacts.splice(i,1) }
    }
  }
}
