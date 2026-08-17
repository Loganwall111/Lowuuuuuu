/** Rare, self-healing open-world rifts with a planetary destination. */
import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import type { LinesMesh } from '@babylonjs/core/Meshes/linesMesh';
import type { Scene } from '@babylonjs/core/scene';
import { PortalSystem, type Portal } from './PortalSystem';
import { generateDimension, makeRng } from './DimensionSystem';

export interface DriftDestination { id:string; position:Vector3; surfaceRadius:number; }
interface ActiveDrift {
  portal:Portal; frame:LinesMesh; destination:DriftDestination;
  age:number; lifetime:number; healFor:number;
}

export class DimensionalDriftSystem {
  private scene:Scene|null=null; private portals:PortalSystem|null=null;
  private active:ActiveDrift|null=null; private clock=0; private nextAt=240;
  private universeSeed=1; private sequence=0;
  attach(scene:Scene,seed:number):void{
    this.dispose();this.scene=scene;this.universeSeed=seed>>>0;this.portals=new PortalSystem(scene);
    const r=makeRng(this.universeSeed^0x77a11f7);this.nextAt=180+r()*720;
  }
  get isOpen():boolean{return !!this.active;}
  update(dt:number,eye:Vector3,star:Vector3|null,destinations:readonly DriftDestination[]):void{
    if(!this.portals||!this.scene||!Number.isFinite(dt)||dt<=0)return;
    this.clock+=dt;this.portals.update(dt,eye);
    if(!this.active&&star&&this.clock>=this.nextAt)this.spawn(star,destinations);
    const a=this.active;if(!a)return;
    a.age+=dt;
    const remaining=a.lifetime-a.age;
    const heal=Math.max(0,Math.min(1,remaining/a.healFor));
    // A time-dilated stitch: the jagged perimeter contracts while the live
    // portal iris closes and all opacity reaches absolute zero.
    if(remaining<a.healFor){a.portal.targetOpenness=heal;a.frame.alpha=heal;
      a.frame.scaling.setAll(.18+.82*heal);a.frame.rotation.z+=dt*(.2+1.4*(1-heal));}
    else a.frame.rotation.z+=dt*.11;
    if(remaining<=0){this.portals.closeAll();a.frame.dispose();this.active=null;
      const r=makeRng((this.universeSeed^++this.sequence)*2654435761);this.nextAt=this.clock+240+r()*1200;}
  }
  private spawn(star:Vector3,destinations:readonly DriftDestination[]):void{
    if(!this.portals||!this.scene)return;
    const seed=(this.universeSeed^Math.imul(++this.sequence,0x9e3779b1))>>>0;
    const rng=makeRng(seed);
    const dir=new Vector3(rng()-.5,(rng()-.5)*.35,rng()-.5).normalize();
    const at=star.add(dir.scale(320+rng()*520));
    const portal=this.portals.createTear(at,dir.scale(-1),18,generateDimension(seed,1));
    portal.lensStrength=3.4;portal.throatMass=2.1;
    const points:Vector3[]=[];
    for(let i=0;i<=40;i++){const a=i/40*Math.PI*2;const rr=19*(.78+rng()*.42);
      points.push(new Vector3(at.x+Math.cos(a)*rr,at.y+Math.sin(a)*rr,at.z));}
    const frame=MeshBuilder.CreateLines('dimensionalDrift',{points,updatable:false},this.scene);
    frame.color=new Color3(.15,.92,1);frame.alpha=1;frame.isPickable=false;
    const picked=destinations.length
      ? destinations[Math.floor(rng()*destinations.length)%destinations.length]
      : {id:'unmapped-'+seed,position:new Vector3((rng()-.5)*180000,(rng()-.5)*18000,(rng()-.5)*180000),surfaceRadius:60};
    this.active={portal,frame,destination:{id:picked.id,position:picked.position.clone(),surfaceRadius:picked.surfaceRadius},
      age:0,lifetime:85+rng()*95,healFor:35+rng()*25};
  }
  tryTransit(player:{position:Vector3;velocity:Vector3}):DriftDestination|null{
    if(!this.active||!this.portals)return null;
    const used=this.portals.tryTransit({position:player.position,velocity:player.velocity,key:'player-drift'});
    return used?this.active.destination:null;
  }
  stats():Record<string,string>{return{'Dimensional drift':this.active?'healing/open':'dormant'};}
  dispose():void{this.portals?.dispose();this.active?.frame.dispose();this.active=null;this.portals=null;this.scene=null;}
}
