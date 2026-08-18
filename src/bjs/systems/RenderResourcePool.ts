/** Reusable CPU staging buffers paired with Babylon GPU buffers. */
const floats=new Map<string,Float32Array>();
const bytes=new Map<string,Uint8Array>();
export function pooledFloat32(key:string,length:number):Float32Array{
 const old=floats.get(key);if(old&&old.length===length)return old;
 const next=new Float32Array(length);floats.set(key,next);return next;
}
export function pooledUint8(key:string,length:number):Uint8Array{
 const old=bytes.get(key);if(old&&old.length===length)return old;
 const next=new Uint8Array(length);bytes.set(key,next);return next;
}
export function releasePoolPrefix(prefix:string):void{
 for(const k of [...floats.keys()])if(k.startsWith(prefix))floats.delete(k);
 for(const k of [...bytes.keys()])if(k.startsWith(prefix))bytes.delete(k);
}
export function renderPoolStats():Record<string,string>{
 let bytesCount=0;for(const a of floats.values())bytesCount+=a.byteLength;for(const a of bytes.values())bytesCount+=a.byteLength;
 return{'Pooled staging buffers':String(floats.size+bytes.size),'Staging memory':(bytesCount/1048576).toFixed(2)+' MB'};
}
