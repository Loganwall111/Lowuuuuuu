// AGENT 16 — FLOW — Traffic Simulation
import { Vehicle } from './Vehicle.js'
import { CONFIG } from '../config.js'

export class TrafficSystem {
  constructor(scene, physics, roads){
    this.scene=scene; this.physics=physics; this.roads=roads
    this.vehicles=[]
  }
  spawn(count= CONFIG.gameplay.trafficCount){
    for(let i=0;i<count;i++){
      const pos=this.roads.getRandomRoadPos()
      pos.y=0.72
      // avoid center player spawn zone
      if(pos.distanceTo(new THREE.Vector3(0,0,72))<38 && i<8) { i--; continue }
      const v=new Vehicle(this.scene, this.physics, pos)
      // give initial yaw along road
      v.yaw= Math.round(Math.random()*4)*Math.PI/2
      v.speed= 6 + Math.random()*6
      this.vehicles.push(v)
    }
    // ensure a Banshee at marina for mission
    const marinaPos=new THREE.Vector3(68,0.72,-520)
    const banshee=new Vehicle(this.scene, this.physics, marinaPos, 0)
    banshee.yaw= -0.6
    banshee.speed=0
    this.vehicles.push(banshee)
    this.missionVehicle=banshee
  }
  update(dt, input, player){
    for(const v of this.vehicles){
      const isPlayerCar = player.isInVehicle && player.currentVehicle===v
      v.update(dt, isPlayerCar? input: null, this.roads)
      // simple avoidance — if too close to another vehicle, brake
      for(const other of this.vehicles){
        if(other===v) continue
        if(v.pos.distanceTo(other.pos)<5.2){
          const toOther=other.pos.clone().sub(v.pos).normalize()
          const fwd=new THREE.Vector3(Math.sin(v.yaw),0,Math.cos(v.yaw))
          if(fwd.dot(toOther)>0.3){
            v.speed *= 0.82
          }
        }
      }
      // avoid player when on foot
      if(!player.isInVehicle && v.pos.distanceTo(player.pos)<3.2 && !v.occupied){
        v.speed *= 0.4
      }
    }
  }
}
