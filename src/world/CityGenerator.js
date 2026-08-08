// AGENT 05 — ATLAS — City Generator Orchestrator
import * as THREE from 'three'
import { Roads } from './Roads.js'
import { Buildings } from './Buildings.js'
import { Vegetation } from './Vegetation.js'
import { Ocean } from './Ocean.js'
import { Weather } from './Weather.js'

export class CityGenerator {
  constructor(scene, engine, physics){
    this.scene=scene
    this.engine=engine
    this.physics=physics
    this.roads=new Roads(scene, physics)
    this.buildings=new Buildings(scene, physics)
    this.vegetation=new Vegetation(scene, physics)
    this.ocean=new Ocean(scene)
    this.weather=new Weather(scene, engine)
  }
  async build(){
    // ground base — asphalt + sidewalks handled by roads, but need large base for physics/shadows
    const groundGeo=new THREE.PlaneGeometry(4000,4000)
    const groundMat=new THREE.MeshStandardMaterial({ color:0x0F1720, roughness:0.96 })
    const ground=new THREE.Mesh(groundGeo, groundMat)
    ground.rotation.x=-Math.PI/2
    ground.position.y=-0.02
    ground.receiveShadow=true
    this.scene.add(ground)

    this.roads.build()
    this.ocean.build()
    this.buildings.build()
    this.vegetation.build(this.buildings.buildings)
    this.weather.build()

    // subtle ambient fog particles
    this._fogPlanes()
  }
  _fogPlanes(){
    const fogMat=new THREE.MeshBasicMaterial({ color:0xFF7EB3, transparent:true, opacity:0.04, depthWrite:false, side:THREE.DoubleSide })
    for(let i=0;i<6;i++){
      const m=new THREE.Mesh(new THREE.PlaneGeometry(1800, 180), fogMat)
      m.position.set((Math.random()-0.5)*1200, 18+Math.random()*24, (Math.random()-0.5)*1000)
      m.rotation.y=Math.random()*Math.PI
      this.scene.add(m)
    }
  }
  update(dt, elapsed){
    this.ocean.update(elapsed)
    this.weather.update(dt, elapsed)
    this.buildings.update(elapsed)
    this.vegetation.update(elapsed)
  }
}
