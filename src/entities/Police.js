// AGENT 13 — BADGE — Police & Wanted System
import * as THREE from 'three'

export class PoliceSystem {
  constructor(scene, physics, roads, audio){
    this.scene=scene; this.physics=physics; this.roads=roads; this.audio=audio
    this.wanted=0
    this.wantedTimer=0
    this.cops=[]
    this.sirenPhase=0
  }
  setWanted(level){
    this.wanted=Math.max(0, Math.min(6, level))
    this._updateHUD()
    if(this.wanted>0 && this.cops.length < this.wanted*2){
      this._spawnCop()
    }
  }
  addWanted(delta){ this.setWanted(this.wanted + delta) }
  _spawnCop(){
    const pos=this.roads.getRandomRoadPos()
    pos.y=0.72
    // ensure not too close to player? caller handles
    const group=new THREE.Group()
    group.position.copy(pos)
    const bodyMat=new THREE.MeshStandardMaterial({ color:0x0F172A, roughness:0.82 })
    const vestMat=new THREE.MeshStandardMaterial({ color:0x1D4ED8, roughness:0.7 })
    const headMat=new THREE.MeshStandardMaterial({ color:0xE8B8A0 })
    const head=new THREE.Mesh(new THREE.SphereGeometry(0.31,10,8), headMat)
    head.position.y=1.72; head.castShadow=true; group.add(head)
    const torso=new THREE.Mesh(new THREE.CapsuleGeometry(0.34,0.58,4,8), bodyMat)
    torso.position.y=1.08; group.add(torso)
    const vest=new THREE.Mesh(new THREE.BoxGeometry(0.62,0.52,0.38), vestMat)
    vest.position.y=1.12; group.add(vest)
    // POLICE text
    const badge=new THREE.Mesh(new THREE.CircleGeometry(0.12,8), new THREE.MeshStandardMaterial({color:0xFFD600, emissive:0xFFD600, emissiveIntensity:1}))
    badge.position.set(0,1.22,0.22); badge.rotation.y=0; group.add(badge)
    const legMat=new THREE.MeshStandardMaterial({color:0x0F172A})
    const l=new THREE.Mesh(new THREE.CapsuleGeometry(0.14,0.55,4,6), legMat); l.position.set(-0.15,0.45,0); group.add(l)
    const r=new THREE.Mesh(new THREE.CapsuleGeometry(0.14,0.55,4,6), legMat); r.position.set(0.15,0.45,0); group.add(r)
    // light bar if car later
    this.scene.add(group)
    const cop={ group, pos:group.position, vel:new THREE.Vector3(), yaw:Math.random()*Math.PI*2, speed:3.8, l,r, state:'chase', cooldown:0 }
    this.cops.push(cop)
  }
  update(dt, elapsed, player){
    // decay wanted if not seen
    if(this.wanted>0){
      this.wantedTimer += dt
      if(this.wantedTimer > 14 + this.wanted*3){
        // if far from cops
        let near=false
        for(const c of this.cops) if(c.pos.distanceTo(player.pos) <42) near=true
        if(!near) this.setWanted(this.wanted-1)
        this.wantedTimer=0
      }
    }
    // update cops
    for(let i=this.cops.length-1;i>=0;i--){
      const c=this.cops[i]
      if(this.wanted===0 && c.pos.distanceTo(player.pos)>80){
        // despawn
        this.scene.remove(c.group)
        this.cops.splice(i,1)
        continue
      }
      const dir=new THREE.Vector3(player.pos.x - c.pos.x, 0, player.pos.z - c.pos.z)
      const dist=dir.length()
      dir.normalize()
      // move
      const spd = this.wanted>3? 5.2: 3.9
      const step=dir.multiplyScalar(spd*dt)
      const next=c.pos.clone().add(step)
      const half=new THREE.Vector3(0.34,0.9,0.34)
      if(!this.physics.checkAABB(next, half).hit){
        c.pos.add(step)
        c.yaw=Math.atan2(dir.x, dir.z)
      } else {
        c.yaw += dt*2
      }
      c.group.position.copy(c.pos)
      c.group.rotation.y=c.yaw
      c.l.rotation.x=Math.sin(elapsed*8)*0.6
      c.r.rotation.x=Math.sin(elapsed*8+Math.PI)*0.6
      // shoot if close
      c.cooldown-=dt
      if(dist<18 && c.cooldown<=0 && this.wanted>0){
        c.cooldown=1.1+Math.random()*0.8
        // damage player
        player.health = Math.max(0, player.health - (6+Math.random()*8))
        // bullet trace
        this._bullet(c.pos.clone().add(new THREE.Vector3(0,1.45,0)), player.pos.clone().add(new THREE.Vector3(0,1.2,0)))
        this.audio?.blip(220,0.1,'square',0.22)
      }
      // siren audio tick
      if(elapsed%1.1 <0.05) this.audio?.siren(elapsed)
    }
    // player health regen tiny if not wanted
    if(this.wanted===0 && player.health<100) player.health=Math.min(100, player.health+dt*2.5)
    this._updateHUD(player)
  }
  _bullet(from,to){
    const dir=to.clone().sub(from).normalize()
    const len=from.distanceTo(to)
    const geo=new THREE.BufferGeometry().setFromPoints([from, to])
    const mat=new THREE.LineBasicMaterial({ color:0xFFD600, transparent:true, opacity:0.85 })
    const line=new THREE.Line(geo, mat)
    this.scene.add(line)
    setTimeout(()=>this.scene.remove(line), 90)
    // impact spark
    const spark=new THREE.Mesh(new THREE.SphereGeometry(0.22,6,6), new THREE.MeshBasicMaterial({color:0xFFD600}))
    spark.position.copy(to)
    this.scene.add(spark)
    setTimeout(()=>this.scene.remove(spark), 120)
  }
  _updateHUD(player){
    const stars=document.querySelectorAll('.star')
    stars.forEach((s,i)=> s.classList.toggle('on', i < this.wanted))
    if(player){
      document.getElementById('bar-health').style.width=`${Math.round(player.health)}%`
      document.getElementById('bar-armor').style.width=`${Math.round(player.armor)}%`
    }
  }
}
