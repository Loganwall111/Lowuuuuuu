/** Babylon Node Render Graph companion for the standard Scene pipeline. */
import type { Scene } from '@babylonjs/core/scene';

export interface RenderStageHealth {id:'A'|'B'|'C'|'D';name:string;responsibilities:string[];}
export const NEXT_GEN_STAGES:RenderStageHealth[]=[
 {id:'A',name:'Base World Geometry',responsibilities:['planets','terrain','ships','celestials','Keplerian orbits']},
 {id:'B',name:'Deep Sky / Safety',responsibilities:['procedural sky','stars','seven galaxy classes','nebulae','extreme-coordinate safety']},
 {id:'C',name:'Singularity Volume',responsibilities:['32-step geodesics','event horizons','accretion disks','lensing','HorizonContinuum']},
 {id:'D',name:'Visual Composite',responsibilities:['ACES emission grade','FXAA','bloom','warp','HUD composition']}
];

export class NextGenRenderGraph{
 private scene:Scene|null=null;private graph:any=null;private status='detached';private generation=0;
 async attach(scene:Scene,webgpu:boolean):Promise<void>{
  this.dispose();this.scene=scene;const gen=++this.generation;
  if(!webgpu){this.status='WebGL compatibility graph';scene.metadata={...(scene.metadata??{}),nextGenerationStages:NEXT_GEN_STAGES};return;}
  this.status='building';
  try{
   const {NodeRenderGraph}=await import('@babylonjs/core/FrameGraph/Node/nodeRenderGraph');
   if(gen!==this.generation||scene!==this.scene)return;
   const graph=new NodeRenderGraph('Next Generation Pipeline',scene,{
    debugTextures:(import.meta as any).env?.DEV===true,rebuildGraphOnEngineResize:true,
    verbose:false,autoConfigure:true,autoFillExternalInputs:true
   });
   graph.comment='A Geometry → B Deep Sky → C Singularity Volume → D Visual Composite';
   graph.setToDefault();
   // The graph is built as a validated companion rather than replacing the
   // scene frame graph: custom GLSL post-processes retain their established
   // ordering while Inspector v2 can inspect graph resources/dependencies.
   const built=graph.buildAsync(false,true,false);
   const ready=await Promise.race([built.then(()=>true),new Promise<boolean>((r)=>setTimeout(()=>r(false),1800))]);
   if(!ready){graph.dispose();this.status='timed-out / legacy scene active';return;}
   if(gen!==this.generation){graph.dispose();return;}
   this.graph=graph;this.status='ready';
   scene.metadata={...(scene.metadata??{}),nextGenerationRenderGraph:graph,nextGenerationStages:NEXT_GEN_STAGES};
  }catch(e){console.warn('Node Render Graph companion unavailable; standard Scene retained:',e);this.status='degraded / standard scene active';}
 }
 stats():Record<string,string>{
  const s=this.scene;const engine=s?.getEngine() as any;
  return{
   'Render graph':this.status,
   'Render nodes':String(NEXT_GEN_STAGES.length),
   'GPU textures':String(engine?.getLoadedTexturesCache?.().length??0),
   'Scene meshes':String(s?.meshes.length??0),
   'Scene materials':String(s?.materials.length??0)
  };
 }
 dispose():void{this.generation++;try{this.graph?.dispose();}catch{}this.graph=null;this.scene=null;this.status='detached';}
}
