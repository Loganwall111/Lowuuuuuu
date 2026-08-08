// AGENT 08 — EDEN — Vegetation, Palms, Props, Street Details
import * as THREE from 'three'

export class Vegetation {
  constructor(scene, physics){
    this.scene=scene
    this.physics=physics
    this.group=new THREE.Group()
    this.scene.add(this.group)
  }
  build(cityBuildings){
    // street palms lining Ocean Drive
    for(let z=-420; z<820; z+=26){
      for(const side of [-1,1]){
        if(Math.random()>0.82) continue
        const x= side* (18+Math.random()*6) + (side===1? 38: -42)
        // avoid building overlap simple check
        let blocked=false
        for(const b of cityBuildings||[]){
          if(Math.abs(b.position.x - x)<14 && Math.abs(b.position.z - z)<14){ blocked=true; break }
        }
        if(blocked) continue
        this._palm(x, z+ (Math.random()-0.5)*6, 0.9+Math.random()*0.45)
      }
    }
    // random scatter palms inland
    for(let i=0;i<90;i++){
      const x=(Math.random()-0.5)*1600
      const z=(Math.random()-0.5)*1600 -80
      if(z < -480) continue
      if(Math.abs(x)<30 && Math.abs(z)<40) continue
      if(Math.random()>0.5) continue
      this._palm(x,z, 0.7+Math.random()*0.55)
    }
    // street props — lights, benches, hydrants, trash
    this._streetProps()
    // traffic lights & signs
    this._trafficLights()
  }
  _palm(x,z,s){
    const group=new THREE.Group()
    group.position.set(x,0,z)
    group.scale.setScalar(s)
    const trunkGeo=new THREE.CylinderGeometry(0.42,0.6,11,7)
    const trunkMat=new THREE.MeshStandardMaterial({ color:0x5C3D1F, roughness:0.94 })
    const trunk=new THREE.Mesh(trunkGeo,trunkMat)
    trunk.position.y=5.5
    trunk.castShadow=true
    trunk.rotation.z=(Math.random()-0.5)*0.2
    group.add(trunk)
    const crownY=11.2
    const leafMat=new THREE.MeshStandardMaterial({ color:0x1B6B3A, roughness:0.85, side:THREE.DoubleSide })
    for(let i=0;i<7;i++){
      const a=i/7*Math.PI*2
      const leaf=new THREE.Mesh(new THREE.CylinderGeometry(0.02,0.9,5.2,5), leafMat)
      leaf.position.set(Math.cos(a)*0.4, crownY, Math.sin(a)*0.4)
      leaf.rotation.z= Math.PI/2 -0.45
      leaf.rotation.y=a
      leaf.castShadow=true
      group.add(leaf)
    }
    // top cluster
    const top=new THREE.Mesh(new THREE.SphereGeometry(1.1,6,5), new THREE.MeshStandardMaterial({color:0x2D5A1E}))
    top.position.y=crownY
    top.scale.y=0.6
    group.add(top)
    this.group.add(group)
    // subtle sway
    group.userData={ sway: Math.random()*Math.PI*2, baseX:x, baseZ:z }
  }
  _streetProps(){
    // lampposts
    for(let x=-800; x<=800; x+=85){
      for(let z=-600; z<=800; z+=85){
        if(Math.random()>0.42) continue
        const lx=x + (Math.random()-0.5)*10
        const lz=z + (Math.random()-0.5)*10
        const post=new THREE.Mesh(new THREE.CylinderGeometry(0.18,0.22,9,8), new THREE.MeshStandardMaterial({color:0x2C3440}))
        post.position.set(lx,4.5,lz)
        post.castShadow=true
        this.group.add(post)
        const head=new THREE.Mesh(new THREE.SphereGeometry(0.9,8,6), new THREE.MeshStandardMaterial({color:0xFFF2A8, emissive:0xFFF2A8, emissiveIntensity:1.2}))
        head.position.set(lx,9.4,lz)
        this.group.add(head)
        const light=new THREE.PointLight(0xFFF2A8, 90, 32)
        light.position.set(lx,9.4,lz)
        this.scene.add(light)
        // small bench sometimes
        if(Math.random()>0.72){
          const bench=new THREE.Mesh(new THREE.BoxGeometry(4.2,0.6,1.1), new THREE.MeshStandardMaterial({color:0x8B7355}))
          bench.position.set(lx+3,0.55,lz+1.5)
          this.group.add(bench)
        }
      }
    }
    // hydrants
    for(let i=0;i<40;i++){
      const x=(Math.random()-0.5)*1400, z=(Math.random()-0.5)*1400
      const hyd=new THREE.Mesh(new THREE.CylinderGeometry(0.45,0.5,1.1,8), new THREE.MeshStandardMaterial({color:0xFF3B30}))
      hyd.position.set(x,0.55,z)
      this.group.add(hyd)
    }
  }
  _trafficLights(){
    for(let x=-800; x<=800; x+=170){
      for(let z=-560; z<=800; z+=170){
        const pole=new THREE.Mesh(new THREE.CylinderGeometry(0.14,0.14,7,6), new THREE.MeshStandardMaterial({color:0x111827}))
        pole.position.set(x,3.5,z)
        this.group.add(pole)
        const box=new THREE.Mesh(new THREE.BoxGeometry(1.6,2.2,0.9), new THREE.MeshStandardMaterial({color:0x0F172A}))
        box.position.set(x,7.2,z)
        this.group.add(box)
        for(let k=0;k<3;k++){
          const col=[0xFF3B30,0xFFD600,0x00E676][k]
          const bulb=new THREE.Mesh(new THREE.SphereGeometry(0.26,6,6), new THREE.MeshStandardMaterial({color:col, emissive:col, emissiveIntensity:k===1? 1.2:0.15}))
          bulb.position.set(x,7.9 -k*0.7, z+0.5)
          this.group.add(bulb)
        }
      }
    }
  }
  update(t){
    this.group.children.forEach(ch=>{
      if(ch.userData?.sway!==undefined){
        ch.rotation.z = Math.sin(t*0.6 + ch.userData.sway)*0.045
        ch.rotation.x = Math.cos(t*0.45 + ch.userData.sway)*0.03
      }
    })
  }
}
