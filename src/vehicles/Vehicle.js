// AGENT 15 — TORQUE — Vehicle Physics (Arcade, drift, damage)
import * as THREE from 'three'
import { CONFIG } from '../config.js'

const CAR_PRESETS=[
  { name:'Banshee GTS', color:0xFF2E8A, emissive:0xFF2E8A, speed:44, body:'low' },
  { name:'Comet S2', color:0x00E5FF, emissive:0x00E5FF, speed:46, body:'sport' },
  { name:'Sultan RS', color:0xFFD600, emissive:0xFFD600, speed:45, body:'tuner' },
  { name:'Elegy RH8', color:0x7C4DFF, emissive:0x7C4DFF, speed:43, body:'sport' },
  { name:'Zentorno', color:0x111827, emissive:0x39FF14, speed:48, body:'hyper' },
  { name:'Adder Vice', color:0xFFEFD5, emissive:0xFF6B4A, speed:47, body:'hyper' },
  { name:'VCPD Cruiser', color:0x0F172A, emissive:0x1D4ED8, speed:42, body:'cruiser' },
]

export class Vehicle {
  constructor(scene, physics, pos, presetIdx=null){
    this.scene=scene; this.physics=physics
    this.preset = CAR_PRESETS[presetIdx!==null? presetIdx : Math.floor(Math.random()*CAR_PRESETS.length)]
    this.name=this.preset.name
    this.pos=pos.clone(); this.pos.y=0.72
    this.vel=new THREE.Vector3()
    this.yaw=Math.random()*Math.PI*2
    this.speed=0
    this.steer=0
    this.occupied=false
    this.driver=null

    this.group=new THREE.Group()
    this.group.position.copy(this.pos)
    this.group.rotation.y=this.yaw
    this.scene.add(this.group)

    this._buildMesh()
    this._addCollider()
  }
  _buildMesh(){
    const color=this.preset.color, emissive=this.preset.emissive
    const bodyMat=new THREE.MeshStandardMaterial({ color, roughness:0.32, metalness:0.58, emissive:0x000000 })
    const glassMat=new THREE.MeshStandardMaterial({ color:0x0B1220, roughness:0.12, metalness:0.85, transparent:true, opacity:0.82 })
    const rubberMat=new THREE.MeshStandardMaterial({ color:0x111111, roughness:0.92 })

    // chassis — different bodies
    let bodyGeo
    if(this.preset.body==='hyper' || this.preset.body==='sport'){
      bodyGeo=new THREE.BoxGeometry(1.96,0.72,4.42)
    } else if(this.preset.body==='cruiser'){
      bodyGeo=new THREE.BoxGeometry(2.02,0.78,4.68)
    } else {
      bodyGeo=new THREE.BoxGeometry(1.92,0.78,4.38)
    }
    const body=new THREE.Mesh(bodyGeo, bodyMat)
    body.position.y=0.68
    body.castShadow=true
    body.receiveShadow=true
    this.group.add(body)
    this.body=body

    // cabin / roof
    const cabin=new THREE.Mesh(new THREE.BoxGeometry(1.72,0.62,1.9), glassMat)
    cabin.position.set(0,1.22, -0.18)
    this.group.add(cabin)

    // windshield slant via scaled box
    const hood=new THREE.Mesh(new THREE.BoxGeometry(1.82,0.12,1.35), bodyMat)
    hood.position.set(0,0.95,1.15)
    hood.rotation.x=0.14
    this.group.add(hood)

    // lights — front
    const headMat=new THREE.MeshStandardMaterial({ color:0xFFF4CC, emissive:0xFFF4CC, emissiveIntensity:1.8 })
    for(const side of [-1,1]){
      const head=new THREE.Mesh(new THREE.BoxGeometry(0.42,0.22,0.12), headMat)
      head.position.set(side*0.72,0.62,2.22)
      this.group.add(head)
      const glow=new THREE.PointLight(0xFFF4CC, 120, 14)
      glow.position.set(side*0.72,0.62,2.6)
      this.group.add(glow)
      this[`head${side>0?'R':'L'}`]=head
    }
    // taillights
    const tailMat=new THREE.MeshStandardMaterial({ color:0xFF2E4A, emissive:0xFF2E4A, emissiveIntensity:1.4 })
    for(const side of [-1,1]){
      const tail=new THREE.Mesh(new THREE.BoxGeometry(0.4,0.18,0.08), tailMat)
      tail.position.set(side*0.72,0.68,-2.20)
      this.group.add(tail)
    }
    // neon underglow for tuner
    if(this.preset.body==='tuner' || this.preset.name.includes('Banshee')){
      const neon=new THREE.Mesh(new THREE.BoxGeometry(1.9,0.04,4.2), new THREE.MeshStandardMaterial({ color:emissive, emissive:emissive, emissiveIntensity:2.2 }))
      neon.position.y=0.18
      this.group.add(neon)
      const under=new THREE.PointLight(emissive, 260, 18)
      under.position.y=0.2
      this.group.add(under)
    }
    // wheels
    this.wheels=[]
    const wheelPos=[[-0.96,0.32,1.32],[0.96,0.32,1.32],[-0.96,0.32,-1.32],[0.96,0.32,-1.32]]
    wheelPos.forEach(([x,y,z])=>{
      const w=new THREE.Mesh(new THREE.CylinderGeometry(0.38,0.38,0.42,14), rubberMat)
      w.rotation.z=Math.PI/2
      w.position.set(x,y,z)
      w.castShadow=true
      this.group.add(w)
      this.wheels.push(w)
    })

    // police livery
    if(this.name.includes('VCPD')){
      const stripe=new THREE.Mesh(new THREE.BoxGeometry(2.03,0.06,4.3), new THREE.MeshStandardMaterial({color:0x1D4ED8}))
      stripe.position.y=0.82
      this.group.add(stripe)
      const text=new THREE.Mesh(new THREE.PlaneGeometry(1.1,0.32), new THREE.MeshBasicMaterial({color:0xFFFFFF}))
      text.position.set(0,0.83,0)
      text.rotation.x=-Math.PI/2
      this.group.add(text)
      const bar=new THREE.Mesh(new THREE.BoxGeometry(1.2,0.14,0.28), new THREE.MeshStandardMaterial({color:0xFF3B30, emissive:0xFF3B30, emissiveIntensity:1.2}))
      bar.position.set(0,1.58,-0.05)
      this.group.add(bar)
      const barBlue=bar.clone()
      barBlue.material=new THREE.MeshStandardMaterial({color:0x1D4ED8, emissive:0x1D4ED8, emissiveIntensity:1.2})
      barBlue.position.x=0.6
      this.group.add(barBlue)
      bar.position.x=-0.6
    }
  }
  _addCollider(){
    const min=new THREE.Vector3(this.pos.x-1.2,0,this.pos.z-2.4)
    const max=new THREE.Vector3(this.pos.x+1.2,1.6,this.pos.z+2.4)
    // dynamic — we update on move via physics check manually
    this._half=new THREE.Vector3(1.05,0.9,2.25)
    this.physics.addBoxCollider(min,max, this.group) // will be updated via override check
    // store index for update
    this._collider=this.physics.colliders[this.physics.colliders.length-1]
  }
  _updateCollider(){
    if(this._collider){
      this._collider.min.set(this.pos.x-1.08,0,this.pos.z-2.2)
      this._collider.max.set(this.pos.x+1.08,1.65,this.pos.z+2.2)
    }
  }
  enter(player){
    this.occupied=true; this.driver=player
  }
  exit(){
    this.occupied=false; this.driver=null; this.speed*=0.72
  }
  update(dt, input, roads){
    const wasOccupied=this.occupied

    // AI cruise if not occupied
    if(!this.occupied){
      // simple follow road graph
      if(!this._target || this.pos.distanceTo(this._target)<6){
        const n=roads.getClosestNode(this.pos)
        // pick random neighbor edge
        const edges=roads.graph.edges.filter(e=> e.from===n.id)
        const choice=edges[Math.floor(Math.random()*edges.length)]
        if(choice){
          const targetNode=roads.graph.nodes.find(nn=> nn.id===choice.to)
          if(targetNode) this._target=new THREE.Vector3(targetNode.x,0.72,targetNode.z)
        }
      }
      if(this._target){
        const dir=new THREE.Vector3(this._target.x - this.pos.x, 0, this._target.z - this.pos.z)
        const ang=Math.atan2(dir.x, dir.z)
        let diff=ang - this.yaw; while(diff>Math.PI) diff-=2*Math.PI; while(diff<-Math.PI) diff+=2*Math.PI
        this.steer = Math.max(-1, Math.min(1, diff*1.6))
        this.speed += (8 - this.speed)*dt*0.9
        if(Math.abs(diff)>0.7) this.speed *= 0.92
      }
    } else if(input){
      // player driving
      let throttle=0, steerIn=0, brake=false
      if(input.isDown('KeyW') || input.isDown('ArrowUp')) throttle=1
      if(input.isDown('KeyS') || input.isDown('ArrowDown')) throttle=-0.78
      if(input.isDown('KeyA') || input.isDown('ArrowLeft')) steerIn=-1
      if(input.isDown('KeyD') || input.isDown('ArrowRight')) steerIn=1
      if(input.isDown('Space')) brake=true

      const maxS=CONFIG.vehicle.maxSpeed
      if(brake){ this.speed *= Math.pow(0.02, dt); if(Math.abs(this.speed)<0.5) this.speed=0 }
      else {
        const accel= CONFIG.vehicle.accel * (throttle>0? 1 : throttle<0? 0.92:0)
        this.speed += throttle * accel * dt
        // drag
        this.speed *= (1 - dt*0.42)
        // limit
        this.speed=Math.max(-maxS*0.45, Math.min(maxS, this.speed))
        if(Math.abs(throttle)<0.01) this.speed *= (1 - dt*1.2)
      }
      this.steer += (steerIn - this.steer)*dt*5.5
      // speed-sensitive steer
      const steerEff = this.steer * CONFIG.vehicle.steer * (1 - Math.min(1, Math.abs(this.speed)/maxS)*0.55)
      this.yaw += steerEff * dt * Math.sign(this.speed) * (Math.abs(this.speed)>1?1:0)
      // drift? if high speed steer + throttle
      if(Math.abs(this.steer)>0.7 && Math.abs(this.speed)>18){
        this.yaw += this.steer*dt*0.8
        // smoke? could add
      }
    }

    // if AI, steer yaw
    if(!this.occupied && this._target){
      const steerEff=this.steer * CONFIG.vehicle.steer *0.55
      this.yaw += steerEff * dt * (this.speed>1?1:0)
    }

    // move
    const fwd=new THREE.Vector3(Math.sin(this.yaw),0,Math.cos(this.yaw))
    const move=fwd.multiplyScalar(this.speed*dt)
    const next=this.pos.clone().add(move)
    // building collision — exclude self
    const half=this._half
    const col=this.physics.checkAABB(next, half, this.group)
    if(col.hit){
      // bounce & reduce speed
      this.speed *= -0.28
      // nudge away
      const pen=col.pen
      if(pen.x < pen.z) next.x += (move.x>0? -pen.x:pen.x)*1.02
      else next.z += (move.z>0? -pen.z:pen.z)*1.02
      // damage flash
      this.body.material.emissive.setHex(0x332222)
      setTimeout(()=> this.body.material.emissive.setHex(0x000000), 180)
    }
    // bounds
    const B=CONFIG.world.size/2 - 10
    next.x=Math.max(-B, Math.min(B, next.x))
    next.z=Math.max(-B, Math.min(B, next.z))

    this.pos.copy(next)
    this.group.position.copy(this.pos)
    this.group.rotation.y=this.yaw
    // wheel spin & steer
    const spin=this.speed*dt*3.2
    this.wheels.forEach((w,i)=>{
      w.rotation.x += spin
      if(i<2){ w.rotation.y = this.steer*0.48 } // front steer
    })
    // lean
    this.group.rotation.z = -this.steer* 0.08 * Math.min(1, Math.abs(this.speed)/22)

    this._updateCollider()

    // light flicker if police
    if(this.name.includes('VCPD')){
      const t=Date.now()*0.008
      const inten= (Math.sin(t*6)>0? 1:0.15) * 2.2
      this.group.children.forEach(c=>{ if(c.isPointLight) c.intensity= 120*inten })
    }
  }
}
