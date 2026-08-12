// AGENT 06 — MERCATOR — Road Network, Intersections, Traffic Graph
import * as THREE from 'three'
import { CONFIG } from '../config.js'

export class Roads {
  constructor(scene, physics){
    this.scene=scene
    this.physics=physics
    this.group=new THREE.Group()
    this.scene.add(this.group)
    this.graph={ nodes:[], edges:[] } // for AI navigation
    this.roadMeshes=[]
  }
  build(){
    const W = CONFIG.world.size
    const roadW = CONFIG.world.roadWidth
    // grid roads
    const step = CONFIG.world.blockSize
    const half = W/2

    // horizontal & vertical roads as long planes
    const roadMat=new THREE.MeshStandardMaterial({ color:0x111827, roughness:0.92, metalness:0.02 })
    const lineMat=new THREE.MeshStandardMaterial({ color:0xFACC15, roughness:0.8 })
    const whiteMat=new THREE.MeshStandardMaterial({ color:0xE5E7EB, roughness:0.9 })

    // create grid lines
    for(let x=-half; x<=half; x+=step){
      const mesh=new THREE.Mesh(new THREE.PlaneGeometry(roadW, W+roadW), roadMat)
      mesh.rotation.x=-Math.PI/2
      mesh.position.set(x, 0.08, 0)
      mesh.receiveShadow=true
      this.group.add(mesh)
      this.roadMeshes.push(mesh)

      // center dashed yellow
      for(let z=-half; z<half; z+=28){
        if(Math.abs(x)<40 && Math.abs(z)<40) continue // skip central intersection extra
        const dash=new THREE.Mesh(new THREE.PlaneGeometry(1.1, 8), lineMat)
        dash.rotation.x=-Math.PI/2
        dash.position.set(x,0.12, z)
        this.group.add(dash)
      }
      // side white lines
      ;[-1,1].forEach(side=>{
        const line=new THREE.Mesh(new THREE.PlaneGeometry(0.55, W), whiteMat)
        line.rotation.x=-Math.PI/2
        line.position.set(x + side*(roadW/2-0.7), 0.11, 0)
        this.group.add(line)
      })
      // graph nodes
      for(let z=-half; z<=half; z+=step){
        this.graph.nodes.push({ x, z, id:`${x},${z}` })
      }
    }
    for(let z=-half; z<=half; z+=step){
      const mesh=new THREE.Mesh(new THREE.PlaneGeometry(W+roadW, roadW), roadMat)
      mesh.rotation.x=-Math.PI/2
      mesh.position.set(0,0.08,z)
      mesh.receiveShadow=true
      this.group.add(mesh)
      this.roadMeshes.push(mesh)
      for(let x=-half; x<half; x+=28){
        const dash=new THREE.Mesh(new THREE.PlaneGeometry(8,1.1), lineMat)
        dash.rotation.x=-Math.PI/2
        dash.position.set(x,0.12,z)
        this.group.add(dash)
      }
      ;[-1,1].forEach(side=>{
        const line=new THREE.Mesh(new THREE.PlaneGeometry(W,0.55), whiteMat)
        line.rotation.x=-Math.PI/2
        line.position.set(0,0.11, z + side*(roadW/2-0.7))
        this.group.add(line)
      })
    }

    // coastal causeway — elevated bridge to south
    const bridgeMat=new THREE.MeshStandardMaterial({ color:0x1F2937, roughness:0.85 })
    const bridge=new THREE.Mesh(new THREE.BoxGeometry(28, 4, 720), bridgeMat)
    bridge.position.set(0, 9, -580)
    bridge.castShadow=true
    bridge.receiveShadow=true
    this.group.add(bridge)
    // pillars
    for(let z=-820; z<-320; z+=84){
      const pillar=new THREE.Mesh(new THREE.CylinderGeometry(2.2,2.8,14,10), new THREE.MeshStandardMaterial({color:0x6B7280}))
      pillar.position.set(0,2,z)
      this.group.add(pillar)
    }

    // sidewalks — slightly higher
    const sideMat=new THREE.MeshStandardMaterial({ color:0x2A3441, roughness:0.96 })
    for(let x=-half+step/2; x<half; x+=step){
      for(let z=-half+step/2; z<half; z+=step){
        const sx=x, sz=z
        const sw=step-roadW-2, sd=step-roadW-2
        if(sw<10 || sd<10) continue
        const side=new THREE.Mesh(new THREE.BoxGeometry(sw, 0.55, sd), sideMat)
        side.position.set(sx,0.32,sz)
        side.receiveShadow=true
        this.group.add(side)
      }
    }

    // intersections — roundabout downtown
    const rbGeo=new THREE.CylinderGeometry(38,38,0.2,32)
    const rbMat=new THREE.MeshStandardMaterial({color:0x0F172A})
    const rb=new THREE.Mesh(rbGeo,rbMat)
    rb.position.set(0,0.14, 40)
    this.group.add(rb)

    // crosswalks
    this._crosswalks()

    // build edges for graph (grid adjacency)
    const nodeMap=new Map(this.graph.nodes.map(n=>[n.id,n]))
    const stepNodes=new Set(this.graph.nodes.map(n=>n.id))
    for(const n of this.graph.nodes){
      const neigh=[[CONFIG.world.blockSize,0],[0,CONFIG.world.blockSize],[-CONFIG.world.blockSize,0],[0,-CONFIG.world.blockSize]]
      for(const [dx,dz] of neigh){
        const id=`${n.x+dx},${n.z+dz}`
        if(stepNodes.has(id)){
          this.graph.edges.push({ from:n.id, to:id })
        }
      }
    }
  }
  _crosswalks(){
    const cwMat=new THREE.MeshStandardMaterial({color:0xFFFFFF, roughness:0.9})
    const half=CONFIG.world.size/2, step=CONFIG.world.blockSize, roadW=CONFIG.world.roadWidth
    for(let x=-half; x<=half; x+=step){
      for(let z=-half; z<=half; z+=step){
        // skip ocean south
        if(z < -620) continue
        for(let i=-2;i<=2;i++){
          const stripe=new THREE.Mesh(new THREE.PlaneGeometry(roadW*0.72, 2.8), cwMat)
          stripe.rotation.x=-Math.PI/2
          stripe.position.set(x + i*4.2, 0.15, z + roadW/2 + 3)
          this.group.add(stripe)
          const stripe2=stripe.clone()
          stripe2.position.set(x + roadW/2 +3, 0.15, z + i*4.2)
          this.group.add(stripe2)
        }
      }
    }
  }
  getRandomRoadPos(){
    const n=this.graph.nodes[Math.floor(Math.random()*this.graph.nodes.length)]
    return new THREE.Vector3(n.x + (Math.random()-0.5)*8, 0.5, n.z + (Math.random()-0.5)*8)
  }
  getClosestNode(pos){
    let best=null, bd=Infinity
    for(const n of this.graph.nodes){
      const d=(n.x-pos.x)**2+(n.z-pos.z)**2
      if(d<bd){ bd=d; best=n }
    }
    return best
  }
}
