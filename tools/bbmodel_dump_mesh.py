#!/usr/bin/env python3
"""Dump triangular Blockbench mesh faces to the compact BakedMesh JSON format."""
import json,sys,math
src,dst=sys.argv[1:3]; d=json.load(open(src)); out={'tris':[],'uvs':[],'normals':[]}
for e in d.get('elements',[]):
 if not e.get('faces') or not e.get('vertices'): continue
 vs=e['vertices']
 for f in e['faces'].values():
  keys=f.get('vertices',[])
  if len(keys)!=3: continue
  p=[[vs[k][i]/16 for i in range(3)] for k in keys]; out['tris'] += sum(p,[])
  a,b,c=p; u=[b[i]-a[i] for i in range(3)];v=[c[i]-a[i] for i in range(3)]; n=[u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0]]; l=math.sqrt(sum(q*q for q in n))or 1;out['normals'] += [q/l for q in n]; out['uvs'] += [0,0,1,0,0,1]
json.dump(out,open(dst,'w'),separators=(',',':'))
