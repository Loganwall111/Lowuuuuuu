// AGENT 07 — MONOLITH — Procedural Buildings, Neon, Interiors
import * as THREE from 'three'
import { CONFIG } from '../config.js'

export class Buildings {
  constructor(scene, physics){
    this.scene=scene
    this.physics=physics
    this.group=new THREE.Group()
    this.scene.add(this.group)
    this.colliders=[]
    this.buildings=[]
  }
  build(){
    const step=CONFIG.world.blockSize
    const half=CONFIG.world.size/2
    const roadW=CONFIG.world.roadWidth

    const districtColors={
      pastel:[0xFFD1DC,0xB8FFF9,0xFFE4B5,0xFFB6C1],
      neon:[0x0A1020,0x1A1F3D,0x0F2A3A,0x141428],
      industrial:[0x374151,0x4B5563,0x6B7280,0x9CA3AF],
      glass:[0x0B1E3A,0x112240,0x0E2A4A,0x1E3A5F]
    }

    const neonPalette=[0xFF2E8A,0x00E5FF,0xFFD600,0x7C4DFF,0x39FF14,0xFF6B4A]

    for(let x=-half+step/2; x<half; x+=step){
      for(let z=-half+step/2; z<half; z+=step){
        if(this._isRoad(x,z)) continue
        if(z < -620) continue // beach — less dense

        // district influence
        let district = this._getDistrict(x,z)
        let heightRange = district? district.height : [12, 42]
        let density = district? district.density : 0.72
        if(Math.random()>density) continue

        const h = heightRange[0] + Math.random()*(heightRange[1]-heightRange[0])
        const w = 18 + Math.random()*22
        const d = 18 + Math.random()*22
        const px = x + (Math.random()-0.5)* (step - roadW - w - 8)
        const pz = z + (Math.random()-0.5)* (step - roadW - d - 8)

        // main box
        const baseColor = district? districtColors[district.palette][Math.floor(Math.random()*4)] : 0x1F2937
        const mat=new THREE.MeshStandardMaterial({
          color: baseColor,
          roughness: district?.palette==='glass'? 0.28:0.82,
          metalness: district?.palette==='glass'? 0.62:0.08,
          emissive: 0x000000
        })
        const geo=new THREE.BoxGeometry(w, h, d)
        const mesh=new THREE.Mesh(geo, mat)
        mesh.position.set(px, h/2+0.55, pz)
        mesh.castShadow=true
        mesh.receiveShadow=true
        this.group.add(mesh)
        this.buildings.push(mesh)

        // collider
        const min=new THREE.Vector3(px - w/2, 0, pz - d/2)
        const max=new THREE.Vector3(px + w/2, h+0.55, pz + d/2)
        this.physics.addBoxCollider(min,max,mesh)

        // windows — emissive planes on facades
        this._addWindows(mesh, w,h,d, baseColor)

        // rooftop details
        if(Math.random()>0.35){
          const topH = 2 + Math.random()*6
          const top = new THREE.Mesh(
            new THREE.BoxGeometry(w*0.48, topH, d*0.48),
            new THREE.MeshStandardMaterial({ color:0x111827, roughness:0.9 })
          )
          top.position.set(px, h+0.55+topH/2, pz)
          top.castShadow=true
          this.group.add(top)
          // antenna / billboard
          if(Math.random()>0.6){
            const billW = w*0.9, billH=6+Math.random()*8
            const canvas=this._neonCanvas()
            const tex=new THREE.CanvasTexture(canvas)
            tex.colorSpace=THREE.SRGBColorSpace
            const billMat=new THREE.MeshStandardMaterial({ map:tex, emissive:0xffffff, emissiveIntensity:0.85, emissiveMap:tex })
            const billboard=new THREE.Mesh(new THREE.PlaneGeometry(billW,billH), billMat)
            billboard.position.set(px, h+topH+ billH/2 +2, pz + d/2+0.6)
            // face camera slightly
            this.group.add(billboard)
            // back light
            const neonLight=new THREE.PointLight(neonPalette[Math.floor(Math.random()*neonPalette.length)], 280, 60)
            neonLight.position.set(px, h+4, pz+d/2+2)
            this.scene.add(neonLight)
          }
        }

        // ground floor neon strip
        if(Math.random()>0.3){
          const col=neonPalette[Math.floor(Math.random()*neonPalette.length)]
          const strip=new THREE.Mesh(
            new THREE.BoxGeometry(w*0.96, 0.9, 0.6),
            new THREE.MeshStandardMaterial({ color:col, emissive:col, emissiveIntensity:2.2 })
          )
          strip.position.set(px, 2.2, pz + d/2+0.35)
          this.group.add(strip)
          const point=new THREE.PointLight(col, 180, 32)
          point.position.set(px, 2.6, pz+d/2+3)
          this.scene.add(point)
        }

        // occasional palm + prop handled by Vegetation agent
      }
    }

    // Landmark — VICE sign downtown (iconic)
    this._addViceLandmark()
    // Ocean View Hotel — enhanced
    this._addOceanViewHotel()
  }
  _isRoad(x,z){
    const step=CONFIG.world.blockSize, roadW=CONFIG.world.roadWidth
    const half=CONFIG.world.size/2
    // check if near grid line
    const rx = Math.abs( ( (x+half)%step) - step/2 )
    const rz = Math.abs( ( (z+half)%step) - step/2 )
    // if close to road center
    return (step/2 - rx) < roadW/2 + 6 || (step/2 - rz) < roadW/2 + 6
  }
  _getDistrict(x,z){
    let best=null, bd=Infinity
    for(const d of CONFIG.world.districts){
      const dx=x-d.center[0], dz=z-d.center[1]
      const dist=Math.sqrt(dx*dx+dz*dz)
      if(dist < d.radius && dist < bd){ bd=dist; best=d }
    }
    return best
  }
  _addWindows(buildingMesh,w,h,d, baseColor){
    const floors=Math.floor(h/3.2)
    const windowGeo=new THREE.PlaneGeometry(1.9, 2.2)
    // random window pattern
    const winColor=new THREE.Color(0xFFF2A8)
    const winMatOff=new THREE.MeshStandardMaterial({ color:0x0B1220, roughness:0.2, metalness:0.6 })
    const winMatOn =new THREE.MeshStandardMaterial({ color:winColor, emissive:winColor, emissiveIntensity:0.95, roughness:0.8 })
    const faces=[
      { axis:'z', sign:1, count: Math.floor(w/3.2) },
      { axis:'z', sign:-1, count: Math.floor(w/3.2) },
      { axis:'x', sign:1, count: Math.floor(d/3.2) },
      { axis:'x', sign:-1, count: Math.floor(d/3.2) },
    ]
    faces.forEach(face=>{
      for(let f=1; f<floors; f++){
        for(let c=0;c<face.count;c++){
          if(Math.random()>0.82) continue // dark
          const y= 2.2 + f*3.2 + (Math.random()-0.5)*0.4
          let x,z, rotY
          if(face.axis==='z'){
            x= buildingMesh.position.x + (c - face.count/2 +0.5)*3.0
            z= buildingMesh.position.z + face.sign*(d/2+0.02)
            rotY= face.sign===1? 0: Math.PI
          } else {
            z= buildingMesh.position.z + (c - face.count/2+0.5)*3.0
            x= buildingMesh.position.x + face.sign*(w/2+0.02)
            rotY= face.sign===1? Math.PI/2: -Math.PI/2
          }
          const mat = Math.random()>0.32? winMatOn : winMatOff
          const win=new THREE.Mesh(windowGeo, mat)
          win.position.set(x,y,z)
          win.rotation.y=rotY
          // slight flicker var
          win.userData={ flicker: Math.random()>0.94 }
          this.group.add(win)
        }
      }
    })
  }
  _neonCanvas(){
    const c=document.createElement('canvas')
    c.width=512; c.height=128
    const ctx=c.getContext('2d')
    ctx.fillStyle='#0A0F1E'
    ctx.fillRect(0,0,c.width,c.height)
    // gradient border
    const g=ctx.createLinearGradient(0,0,512,0)
    g.addColorStop(0,'#FF2E8A'); g.addColorStop(0.5,'#FFD600'); g.addColorStop(1,'#00E5FF')
    ctx.strokeStyle=g; ctx.lineWidth=8; ctx.strokeRect(4,4,504,120)
    ctx.font='bold 56px Bebas Neue, sans-serif'
    ctx.textAlign='center'; ctx.textBaseline='middle'
    const phrases=['VICE CITY','LEONIDA','MALIBU CLUB','SUNDOWN MOTEL','BANSHEE','AMMU-NATION','OCEAN VIEW']
    const phrase=phrases[Math.floor(Math.random()*phrases.length)]
    ctx.shadowColor='#FF2E8A'; ctx.shadowBlur=18
    ctx.fillStyle='#FFFFFF'
    ctx.fillText(phrase,256,64)
    // scanline
    ctx.shadowBlur=0
    ctx.fillStyle='rgba(0,229,255,0.12)'
    for(let y=0;y<128;y+=4) ctx.fillRect(0,y,512,1)
    return c
  }
  _addViceLandmark(){
    // Huge VICE sign on beach hotel row
    const group=new THREE.Group()
    group.position.set(42, 0, -420)
    const podium=new THREE.Mesh(
      new THREE.BoxGeometry(72, 42, 28),
      new THREE.MeshStandardMaterial({ color:0xFDE68A, roughness:0.85 })
    )
    podium.position.y=21
    podium.castShadow=true
    group.add(podium)
    // VICE letters as extruded neon
    const colors=[0xFF2E8A,0x00E5FF,0xFFD600,0x7C4DFF]
    'VICE'.split('').forEach((ch,i)=>{
      const col=colors[i%colors.length]
      const letter=new THREE.Mesh(
        new THREE.BoxGeometry(11, 18, 2.2),
        new THREE.MeshStandardMaterial({ color:col, emissive:col, emissiveIntensity:1.8 })
      )
      letter.position.set(-22 + i*14.5, 52, 15)
      group.add(letter)
      const light=new THREE.PointLight(col, 420, 90)
      light.position.set(-22+i*14.5,52,22)
      group.add(light)
    })
    // CITY subtext
    const cityText=new THREE.Mesh(
      new THREE.PlaneGeometry(42,9),
      new THREE.MeshStandardMaterial({ color:0xFFFFFF, emissive:0xFFFFFF, emissiveIntensity:0.6 })
    )
    cityText.position.set(0,39,15.2)
    group.add(cityText)

    this.scene.add(group)
    // collider for podium
    this.physics.addBoxCollider(new THREE.Vector3(6,0,-434), new THREE.Vector3(78,42,-406))
  }
  _addOceanViewHotel(){
    const h=64, w=52, d=34
    const x=-280, z=-460
    const hotel=new THREE.Mesh(
      new THREE.BoxGeometry(w,h,d),
      new THREE.MeshStandardMaterial({ color:0xFFEFD5, roughness:0.88 })
    )
    hotel.position.set(x, h/2+0.5, z)
    hotel.castShadow=true
    hotel.receiveShadow=true
    this.scene.add(hotel)
    this.physics.addBoxCollider(new THREE.Vector3(x-w/2,0,z-d/2), new THREE.Vector3(x+w/2,h, z+d/2))
    // balconies
    for(let f=0; f<10; f++){
      const bal=new THREE.Mesh(new THREE.BoxGeometry(w*0.92,0.9,3.2), new THREE.MeshStandardMaterial({color:0xFFF8E7}))
      bal.position.set(x, 8+f*5.4, z+d/2+1.6)
      this.scene.add(bal)
    }
    // sign
    const sign=new THREE.Mesh(
      new THREE.BoxGeometry(28,7,1.2),
      new THREE.MeshStandardMaterial({ color:0x00E5FF, emissive:0x00E5FF, emissiveIntensity:1.6 })
    )
    sign.position.set(x, 58, z+d/2+1)
    this.scene.add(sign)
    const light=new THREE.PointLight(0x00E5FF, 520, 120)
    light.position.set(x,54,z+d/2+6)
    this.scene.add(light)
  }
  update(t){
    // window flicker
    this.group.children.forEach(m=>{
      if(m.userData?.flicker){
        if(Math.random()<0.02) m.material.emissiveIntensity = Math.random()>0.5? 0.15:1.1
      }
    })
  }
}
