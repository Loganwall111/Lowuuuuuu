// AGENT 12 — CROWD — Pedestrian AI
import * as THREE from 'three'
import { CONFIG } from '../config.js'

function randomColor(){
  const palettes=[[0xFFD6E0,0xB8FFF9,0xFFF4BD,0xD0BCFF],[0x222B3A,0x3A2F2F,0x2F3A2F,0x1F2A44]]
  const p=palettes[Math.floor(Math.random()*palettes.length)]
  return p[Math.floor(Math.random()*p.length)]
}

export class PedestrianSystem {
  constructor(scene, physics, roads){
    this.scene=scene; this.physics=physics; this.roads=roads
    this.peds=[]
  }
  spawn(count= CONFIG.gameplay.pedCount){
    for(let i=0;i<count;i++){
      const pos=this.roads.getRandomRoadPos()
      // offset to sidewalk
      pos.x += (Math.random()-0.5)*12
      pos.z += (Math.random()-0.5)*12
      pos.y=0.92
      this._createPed(pos)
    }
  }
  _createPed(pos){
    const group=new THREE.Group()
    group.position.copy(pos)
    const bodyMat=new THREE.MeshStandardMaterial({ color: randomColor(), roughness:0.85 })
    const skinMat=new THREE.MeshStandardMaterial({ color:0xE8B8A0, roughness:0.9 })
    const head=new THREE.Mesh(new THREE.SphereGeometry(0.3,10,8), skinMat)
    head.position.y=1.68; head.castShadow=true; group.add(head)
    const torso=new THREE.Mesh(new THREE.CapsuleGeometry(0.32,0.5,4,8), bodyMat)
    torso.position.y=1.05; torso.castShadow=true; group.add(torso)
    const legMat=new THREE.MeshStandardMaterial({color:0x2A2F3A})
    const leftLeg=new THREE.Mesh(new THREE.CapsuleGeometry(0.13,0.52,4,6), legMat)
    leftLeg.position.set(-0.14,0.42,0); group.add(leftLeg)
    const rightLeg=new THREE.Mesh(new THREE.CapsuleGeometry(0.13,0.52,4,6), legMat)
    rightLeg.position.set(0.14,0.42,0); group.add(rightLeg)
    // store legs for anim
    this.scene.add(group)
    const ped={
      group, pos:group.position, vel:new THREE.Vector3(),
      yaw: Math.random()*Math.PI*2,
      speed: 0.9 + Math.random()*1.2,
      target: this.roads.getRandomRoadPos(),
      state:'wander',
      leftLeg,rightLeg, torso, panic:0, elapsed:Math.random()*10
    }
    ped.target.y=0.92
    this.peds.push(ped)
  }
  update(dt, elapsed, playerPos, wantedLevel){
    for(const p of this.peds){
      p.elapsed+=dt
      // panic if wanted or player near and shooting
      const distToPlayer=p.pos.distanceTo(playerPos)
      if(wantedLevel>1 && distToPlayer<42) p.panic=1
      if(p.panic>0) p.panic-=dt*0.25

      let target = p.target
      if(p.panic>0){
        // flee away from player
        const fleeDir=p.pos.clone().sub(playerPos).normalize()
        target=p.pos.clone().add(fleeDir.multiplyScalar(18))
        p.speed=3.2
      } else {
        p.speed=1.0 + Math.sin(p.elapsed*0.3)*0.2
      }

      const dir=new THREE.Vector3(target.x - p.pos.x, 0, target.z - p.pos.z)
      const dlen=dir.length()
      if(dlen<2.2){
        p.target=this.roads.getRandomRoadPos()
        p.target.y=0.92
        continue
      }
      dir.normalize().multiplyScalar(p.speed*dt* (p.panic>0? 1.8:1))
      // avoid buildings
      const next=p.pos.clone().add(dir)
      const half=new THREE.Vector3(0.32,0.9,0.32)
      const col=this.physics.checkAABB(next, half)
      if(col.hit){
        // turn
        p.yaw += (Math.random()-0.5)*1.2 + Math.PI/2
        p.target=this.roads.getRandomRoadPos()
        continue
      }
      p.pos.add(dir)
      if(dir.lengthSq()>1e-6){
        const yaw=Math.atan2(dir.x, dir.z)
        let diff=yaw - p.yaw; while(diff>Math.PI) diff-=2*Math.PI; while(diff<-Math.PI) diff+=2*Math.PI
        p.yaw += diff*dt*4
      }
      p.group.position.copy(p.pos)
      p.group.rotation.y=p.yaw
      // walk cycle
      const s = p.panic>0? 9: 5.2
      p.leftLeg.rotation.x=Math.sin(elapsed*s + p.elapsed)*0.62
      p.rightLeg.rotation.x=Math.sin(elapsed*s + p.elapsed+Math.PI)*0.62
      p.group.position.y=0.92 + Math.abs(Math.sin(elapsed*s))*0.04
    }
  }
}
