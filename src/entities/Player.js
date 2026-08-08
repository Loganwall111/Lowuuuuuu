// AGENT 11 — LUCIA — Player Controller (Third Person, Parkour, Combat Stance)
import * as THREE from 'three'
import { CONFIG } from '../config.js'

export class Player {
  constructor(scene, camera, input, physics, audio){
    this.scene=scene; this.camera=camera; this.input=input; this.physics=physics; this.audio=audio
    this.group=new THREE.Group()
    this.scene.add(this.group)

    this.pos=new THREE.Vector3(0, 0.95, 72)
    this.vel=new THREE.Vector3()
    this.yaw= -0.2
    this.pitch= -0.08
    this.camDist= 7.2
    this.camHeight= 1.72
    this.onGround=true
    this.health=100; this.armor=44; this.stamina=68
    this.isAiming=false; this.isSprinting=false; this.isInVehicle=false
    this.currentVehicle=null
    this.weapon='PISTOL .50'
    this.ammo=12; this.reserve=84

    this._buildModel()
    this._updateCamera(0)
  }
  _buildModel(){
    // Lucia-inspired stylized model — primitives + PBR, shadow casting
    const bodyMat=new THREE.MeshStandardMaterial({ color:0xFF2E8A, roughness:0.72 })
    const skinMat=new THREE.MeshStandardMaterial({ color:0xE8B8A0, roughness:0.85 })
    const jeansMat=new THREE.MeshStandardMaterial({ color:0x1E3A8A, roughness:0.9 })
    const hairMat=new THREE.MeshStandardMaterial({ color:0x1A0F0A, roughness:0.9 })

    this.model=new THREE.Group()

    const torso=new THREE.Mesh(new THREE.CapsuleGeometry(0.38,0.62,4,10), bodyMat)
    torso.position.y=1.12
    torso.castShadow=true
    this.model.add(torso)

    const head=new THREE.Mesh(new THREE.SphereGeometry(0.33,14,12), skinMat)
    head.position.y=1.78
    head.castShadow=true
    this.model.add(head)

    const hair=new THREE.Mesh(new THREE.SphereGeometry(0.36,12,10), hairMat)
    hair.position.set(0,1.84,-0.08)
    hair.scale.set(1,0.85,1.1)
    this.model.add(hair)

    const armGeo=new THREE.CapsuleGeometry(0.11,0.52,4,8)
    const leftArm=new THREE.Mesh(armGeo, skinMat)
    leftArm.position.set(-0.48,1.08,0)
    leftArm.castShadow=true
    this.model.add(leftArm)
    const rightArm=new THREE.Mesh(armGeo, skinMat)
    rightArm.position.set(0.48,1.08,0)
    rightArm.castShadow=true
    this.model.add(rightArm)
    this.rightArm=rightArm; this.leftArm=leftArm

    const legGeo=new THREE.CapsuleGeometry(0.16,0.62,4,8)
    const leftLeg=new THREE.Mesh(legGeo, jeansMat)
    leftLeg.position.set(-0.18,0.42,0)
    leftLeg.castShadow=true
    this.model.add(leftLeg)
    const rightLeg=new THREE.Mesh(legGeo, jeansMat)
    rightLeg.position.set(0.18,0.42,0)
    rightLeg.castShadow=true
    this.model.add(rightLeg)
    this.leftLeg=leftLeg; this.rightLeg=rightLeg

    // shadow blob
    const blob=new THREE.Mesh(new THREE.CircleGeometry(0.62,16), new THREE.MeshBasicMaterial({ color:0x000000, transparent:true, opacity:0.28, depthWrite:false }))
    blob.rotation.x=-Math.PI/2
    blob.position.y=0.04
    this.model.add(blob)

    this.group.add(this.model)
    this.group.position.copy(this.pos)
  }
  tryEnterVehicle(vehicles){
    if(this.isInVehicle) return false
    let best=null, bd=3.8
    for(const v of vehicles){
      const d=this.pos.distanceTo(v.pos)
      if(d<bd){ bd=d; best=v }
    }
    if(best){
      this.isInVehicle=true
      this.currentVehicle=best
      best.enter(this)
      this.model.visible=false
      this._toast(`Entered ${best.name}`)
      return true
    }
    return false
  }
  exitVehicle(){
    if(!this.isInVehicle || !this.currentVehicle) return
    const v=this.currentVehicle
    const off=new THREE.Vector3(2.6,0,0).applyAxisAngle(new THREE.Vector3(0,1,0), v.yaw)
    this.pos.copy(v.pos).add(off)
    this.pos.y=0.95
    this.isInVehicle=false
    this.currentVehicle.exit()
    this.currentVehicle=null
    this.model.visible=true
    this.vel.set(0,0,0)
  }
  _toast(msg){
    const t=document.getElementById('toast')
    if(!t) return
    t.textContent=msg
    t.classList.add('show')
    setTimeout(()=>t.classList.remove('show'), 2200)
  }
  update(dt, elapsed){
    if(this.isInVehicle && this.currentVehicle){
      // sync camera to vehicle
      this.pos.copy(this.currentVehicle.pos)
      this.yaw=this.currentVehicle.yaw
      this._updateCamera(dt)
      // speedo update
      const kmh=Math.abs(this.currentVehicle.speed*18)
      const el=document.getElementById('speedo-val')
      if(el) el.textContent=Math.round(kmh).toString().padStart(2,'0')
      document.getElementById('speedbar-fill').style.width=`${Math.min(100, kmh/1.8)}%`
      if(this.input.isDown('KeyE')){
        // debounce
        if(!this._exitCooldown || elapsed - this._exitCooldown >0.5){
          this._exitCooldown=elapsed
          this.exitVehicle()
        }
      }
      if(this.input.isDown('KeyQ') && (!this._qCd || elapsed-this._qCd>0.5)){
        this._qCd=elapsed
        const e=new CustomEvent('radio-switch')
        window.dispatchEvent(e)
      }
      return
    }

    // mouse look
    const md=this.input.consumeMouseDelta()
    const sens=0.0024
    this.yaw -= md.dx * sens
    this.pitch -= md.dy * sens
    this.pitch=Math.max(-0.72, Math.min(0.62, this.pitch))
    // wheel zoom
    const wheel=this.input.consumeWheel()
    if(wheel!==0){ this.camDist = Math.max(3.2, Math.min(12, this.camDist + wheel*0.0025)) }

    // input move
    const fwd=new THREE.Vector3(Math.sin(this.yaw),0,Math.cos(this.yaw))
    const right=new THREE.Vector3(Math.cos(this.yaw),0,-Math.sin(this.yaw))
    let move=new THREE.Vector3()
    if(this.input.isDown('KeyW')) move.add(fwd)
    if(this.input.isDown('KeyS')) move.sub(fwd)
    if(this.input.isDown('KeyA')) move.sub(right)
    if(this.input.isDown('KeyD')) move.add(right)
    const hasInput=move.lengthSq()>0.001
    if(hasInput) move.normalize()

    const sprint=this.input.isDown('ShiftLeft') && hasInput && this.stamina>0
    this.isSprinting=sprint
    if(sprint) this.stamina=Math.max(0,this.stamina - dt*18)
    else this.stamina=Math.min(100, this.stamina + dt*12)

    const speed= sprint? CONFIG.player.sprint : CONFIG.player.speed
    const accel= hasInput? speed : 0
    // simple velocity
    const targetVel=move.multiplyScalar(accel)
    // lerp for inertia
    this.vel.x += (targetVel.x - this.vel.x)* dt*9
    this.vel.z += (targetVel.z - this.vel.z)* dt*9

    // gravity / jump
    if(!this.onGround) this.vel.y -= CONFIG.player.gravity * dt
    if(this.input.isDown('Space') && this.onGround){
      this.vel.y = CONFIG.player.jump
      this.onGround=false
      this.audio?.blip(320,0.18,'sine',0.18)
    }

    // integrate
    const nextPos=this.pos.clone().add(new THREE.Vector3(this.vel.x, this.vel.y, this.vel.z).multiplyScalar(dt))

    // ground clamp
    if(nextPos.y <= 0.95){
      nextPos.y=0.95
      this.vel.y=0
      this.onGround=true
    } else {
      this.onGround=false
    }

    // building collision — push back
    const half=new THREE.Vector3(0.38,0.9,0.38)
    const col=this.physics.checkAABB(nextPos, half)
    if(col.hit){
      // slide along smallest penetration
      const pen=col.pen
      if(pen.x < pen.z && pen.x < pen.y){ nextPos.x += (this.vel.x>0? -pen.x: pen.x); this.vel.x=0 }
      else if(pen.z < pen.y){ nextPos.z += (this.vel.z>0? -pen.z: pen.z); this.vel.z=0 }
      else { nextPos.y += pen.y; this.vel.y=0 }
    }

    // world bounds
    const B=CONFIG.world.size/2 - 18
    nextPos.x=Math.max(-B, Math.min(B, nextPos.x))
    nextPos.z=Math.max(-B, Math.min(B, nextPos.z))

    this.pos.copy(nextPos)
    this.group.position.copy(this.pos)
    // face move direction
    if(hasInput && !this.isAiming){
      const dir=Math.atan2(this.vel.x, this.vel.z)
      // lerp model yaw
      let diff=dir - this.model.rotation.y
      while(diff>Math.PI) diff-=2*Math.PI
      while(diff<-Math.PI) diff+=2*Math.PI
      this.model.rotation.y += diff*dt*9
    } else if(this.isAiming){
      this.model.rotation.y=this.yaw
    }

    // bob animation
    const bobSpeed= sprint? 11: hasInput? 7: 0
    if(bobSpeed>0){
      const bob=Math.sin(elapsed*bobSpeed)*0.045
      this.model.position.y=bob
      this.leftLeg.rotation.x=Math.sin(elapsed*bobSpeed)*0.55
      this.rightLeg.rotation.x=Math.sin(elapsed*bobSpeed+Math.PI)*0.55
      this.leftArm.rotation.x=Math.sin(elapsed*bobSpeed+Math.PI)*0.45
      this.rightArm.rotation.x=Math.sin(elapsed*bobSpeed)*0.45
    } else {
      this.model.position.y += (0 - this.model.position.y)*dt*6
      this.leftLeg.rotation.x*=0.92; this.rightLeg.rotation.x*=0.92
    }

    // aiming
    this.isAiming=this.input.mouse.right || this.input.isDown('KeyQ')
    document.getElementById('reticle')?.classList.toggle('show', this.isAiming)
    document.getElementById('speedo')?.classList.remove('visible')

    // vehicle enter
    if(this.input.isDown('KeyE') && (!this._enterCd || elapsed - this._enterCd>0.6)){
      // will be handled in main loop with vehicles list — we also attempt here if global not yet
      this._enterCd=elapsed
      // emit event for main to handle with vehicles
      window.dispatchEvent(new CustomEvent('player-try-enter'))
    }

    // shooting
    if(this.input.mouse.left && (!this._shootCd || elapsed - this._shootCd>0.14)){
      this._shootCd=elapsed
      window.dispatchEvent(new CustomEvent('player-shoot', { detail:{ pos:this.pos.clone().add(new THREE.Vector3(0,1.45,0)), dir:new THREE.Vector3(Math.sin(this.yaw), this.pitch*0.9, Math.cos(this.yaw)).normalize() }}))
      // recoil
      this.rightArm.rotation.x -=0.6
      // ammo
      if(this.ammo>0) this.ammo--
      else { this.ammo=12; this.reserve-=12; this.audio?.blip(140,0.28,'square',0.2) }
    }

    // ui bars
    document.getElementById('bar-stamina').style.width=`${Math.round(this.stamina)}%`
    document.getElementById('weapon-name').textContent=this.weapon
    document.getElementById('ammo-count').textContent=String(this.ammo)

    this._updateCamera(dt)
  }
  _updateCamera(dt){
    // third person orbit — GTA VI style, slightly low, cinematic
    const idealDist=this.isAiming? 3.8 : this.isInVehicle? 9.5 : this.camDist
    const idealHeight=this.isAiming? 1.62 : this.camHeight
    const yaw=this.yaw, pitch=this.pitch

    // sphere offset
    const horiz=Math.cos(pitch)*idealDist
    const camOffset=new THREE.Vector3(
      -Math.sin(yaw)*horiz,
      idealHeight + Math.sin(pitch)*idealDist*0.9,
      -Math.cos(yaw)*horiz
    )
    const targetCamPos=this.pos.clone().add(new THREE.Vector3(0,1.45,0)).add(camOffset)

    // raycast camera collision to avoid clipping through buildings
    const dir=targetCamPos.clone().sub(this.pos.clone().add(new THREE.Vector3(0,1.45,0))).normalize()
    const maxDist=targetCamPos.distanceTo(this.pos.clone().add(new THREE.Vector3(0,1.45,0)))
    const hit=this.physics.raycast(this.pos.clone().add(new THREE.Vector3(0,1.2,0)), dir, maxDist)
    let finalPos=targetCamPos
    if(hit){
      finalPos=hit.point.clone().sub(dir.clone().multiplyScalar(0.55))
    }

    this.camera.position.lerp(finalPos, 1 - Math.pow(0.001, dt))
    // look at
    const lookAtPos=this.pos.clone().add(new THREE.Vector3(0,1.38,0)).add(new THREE.Vector3(Math.sin(yaw)*1.2, pitch*2+0.3, Math.cos(yaw)*1.2))
    // smooth look
    const curDir=new THREE.Vector3()
    this.camera.getWorldDirection(curDir)
    const desiredDir=lookAtPos.clone().sub(this.camera.position).normalize()
    // slerp via quaternion
    const targetQuat=new THREE.Quaternion().setFromUnitVectors(new THREE.Vector3(0,0,-1), desiredDir)
    // but we can just lerp lookAt
    this.camera.lookAt(lookAtPos)
  }
}
