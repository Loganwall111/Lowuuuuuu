// AGENT 09 — POSEIDON — Ocean & Beach
import * as THREE from 'three'

export class Ocean {
  constructor(scene){
    this.scene=scene
    this.group=new THREE.Group()
    this.scene.add(this.group)
  }
  build(){
    // Sand beach — huge plane south side
    const sandGeo=new THREE.PlaneGeometry(2400, 900, 32, 32)
    // add dunes
    const pos=sandGeo.attributes.position
    for(let i=0;i<pos.count;i++){
      const x=pos.getX(i), y=pos.getY(i)
      const d = Math.abs(y) // distance from center line
      const wave = Math.sin(x*0.008)*6 + Math.cos(x*0.015 + y*0.01)*4
      const dune = Math.max(0, (420 - d))*0.02
      pos.setZ(i, wave*0.35 + dune + (Math.random()*1.2))
    }
    pos.needsUpdate=true
    sandGeo.computeVertexNormals()
    const sandMat=new THREE.MeshStandardMaterial({ color:0xF5E6C8, roughness:0.98, metalness:0 })
    const sand=new THREE.Mesh(sandGeo, sandMat)
    sand.rotation.x=-Math.PI/2
    sand.position.set(0, -0.4, -820)
    sand.receiveShadow=true
    this.group.add(sand)

    // Ocean — shader plane
    const oceanGeo=new THREE.PlaneGeometry(3600, 3600, 120, 120)
    const oceanMat=new THREE.ShaderMaterial({
      uniforms:{
        uTime:{value:0},
        uColorShallow:{value:new THREE.Color(0x00E5FF)},
        uColorDeep:{value:new THREE.Color(0x001A4D)},
        uSunDir:{value:new THREE.Vector3(0.5,0.8,-0.3).normalize()},
      },
      vertexShader:`
        varying vec2 vUv;
        varying float vH;
        uniform float uTime;
        void main(){
          vUv=uv;
          vec3 p=position;
          float t=uTime*0.6;
          float w1=sin(p.x*0.008 + t)*6.0;
          float w2=sin(p.x*0.015 - t*0.7 + p.y*0.01)*4.0;
          float w3=sin(p.y*0.012 + t*0.5)*3.5;
          p.z += w1+w2+w3;
          vH = (w1+w2+w3);
          gl_Position=projectionMatrix*modelViewMatrix*vec4(p,1.0);
        }
      `,
      fragmentShader:`
        varying vec2 vUv;
        varying float vH;
        uniform vec3 uColorShallow;
        uniform vec3 uColorDeep;
        uniform vec3 uSunDir;
        uniform float uTime;
        void main(){
          float depth = smoothstep(0.2, 0.9, vUv.y);
          vec3 col = mix(uColorShallow, uColorDeep, depth);
          // wave highlight
          float spark = pow(max(0.0, sin(vUv.x*80.0 + uTime*6.0)*0.5+0.5), 32.0)*0.25;
          col += spark;
          // fresnel
          float fres = pow(1.0 - max(0.0, dot(vec3(0,1,0), uSunDir)), 2.0)*0.35;
          col += fres * vec3(1.0,0.9,0.7);
          // foam near beach (vUv.y ~ 0.78 is shoreline)
          float foam = smoothstep(0.74, 0.78, vUv.y) * (1.0 - smoothstep(0.78, 0.86, vUv.y));
          foam *= 0.6 + 0.4*sin(vUv.x*30.0 + uTime*2.0);
          col = mix(col, vec3(1.0), foam*0.9);
          gl_FragColor=vec4(col,1.0);
        }
      `,
      side:THREE.DoubleSide
    })
    const ocean=new THREE.Mesh(oceanGeo, oceanMat)
    ocean.rotation.x=-Math.PI/2
    ocean.position.set(0, -2.2, -1450)
    this.group.add(ocean)
    this.oceanMat=oceanMat

    // Beach palms line
    this._addPalms()

    // distant islands / haze
    const hazeGeo=new THREE.PlaneGeometry(4000, 600)
    const hazeMat=new THREE.MeshBasicMaterial({ color:0xFF8FB3, transparent:true, opacity:0.18, side:THREE.DoubleSide, depthWrite:false })
    const haze=new THREE.Mesh(hazeGeo,hazeMat)
    haze.position.set(0, 140, -2450)
    this.group.add(haze)
  }
  _addPalms(){
    const trunkGeo=new THREE.CylinderGeometry(0.45,0.62,12,8)
    const trunkMat=new THREE.MeshStandardMaterial({color:0x5A3A22, roughness:0.9})
    const leafMat=new THREE.MeshStandardMaterial({color:0x1B7A3D, roughness:0.8})
    for(let i=0;i<52;i++){
      const x = (Math.random()-0.5)*2100
      const z = -520 - Math.random()*360
      const g=new THREE.Group()
      const trunk=new THREE.Mesh(trunkGeo,trunkMat)
      trunk.position.y=6
      // lean a little
      trunk.rotation.z=(Math.random()-0.5)*0.18
      trunk.rotation.x=(Math.random()-0.5)*0.12
      trunk.castShadow=true
      g.add(trunk)
      for(let j=0;j<5;j++){
        const leafGeo=new THREE.SphereGeometry(2.2,6,4)
        leafGeo.scale(1,0.28,1.6)
        const leaf=new THREE.Mesh(leafGeo,leafMat)
        const a=j/5*Math.PI*2
        leaf.position.set(Math.cos(a)*1.6, 12.2, Math.sin(a)*1.6)
        leaf.rotation.y=a
        leaf.rotation.z=0.35
        g.add(leaf)
      }
      g.position.set(x,0,z)
      g.scale.setScalar(0.9+Math.random()*0.5)
      this.group.add(g)
    }
  }
  update(t){
    if(this.oceanMat) this.oceanMat.uniforms.uTime.value=t
  }
}
