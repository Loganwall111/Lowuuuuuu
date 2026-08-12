// AGENT 18 — CARTO — Minimap & Radar
import * as THREE from 'three'

export class Minimap {
  constructor(roads, buildings){
    this.canvas=document.getElementById('minimap')
    this.ctx=this.canvas?.getContext('2d')
    this.roads=roads
    this.buildings=buildings
    this.size=168
    this.range=220
    // offscreen cache of static map
    this._staticCache=null
  }
  _drawStatic(){
    if(!this.ctx) return
    const ctx=this.ctx
    const s=this.size
    ctx.clearRect(0,0,s,s)
    // bg
    const g=ctx.createRadialGradient(s/2,s/2,0,s/2,s/2,s/2)
    g.addColorStop(0,'#0f1b2e'); g.addColorStop(1,'#070d1a')
    ctx.fillStyle=g; ctx.fillRect(0,0,s,s)

    // roads — draw global then clip
    ctx.strokeStyle='rgba(55,65,81,0.9)'
    ctx.lineWidth=1.2
    // we draw all roads as lines — use graph edges approximate
    // instead draw grid
    ctx.strokeStyle='rgba(100,116,139,0.75)'
    ctx.lineWidth=1
    const half=900
    const step=85
    for(let x=-half;x<=half;x+=step){
      const sx=(x+half)/1800 * 520 -180 // map to canvas approx
      // simplify: draw vertical lines across
      ctx.beginPath()
      ctx.moveTo( s/2 + x*0.06, 4)
      ctx.lineTo( s/2 + x*0.06, s-4)
      ctx.stroke()
    }
    for(let z=-half;z<=half;z+=step){
      const sy=(z+half)/1800 * 520 -180
      ctx.beginPath()
      ctx.moveTo(4, s/2 + z*0.06)
      ctx.lineTo(s-4, s/2 + z*0.06)
      ctx.stroke()
    }

    // buildings as small rects
    ctx.fillStyle='rgba(148,163,184,0.28)'
    if(this.buildings){
      for(const b of this.buildings.slice(0,180)){
        const x=s/2 + b.position.x*0.06
        const y=s/2 + b.position.z*0.06
        ctx.fillRect(x-1.2, y-1.2, 2.4,2.4)
      }
    }

    // beach & ocean
    ctx.fillStyle='rgba(245,230,200,0.22)'
    ctx.fillRect(0, s/2 + (-820)*0.06, s, 40)
    ctx.fillStyle='rgba(0,229,255,0.14)'
    ctx.fillRect(0, s/2 + (-1450)*0.06, s, 90)

    // border
    ctx.strokeStyle='rgba(255,255,255,0.06)'
    ctx.lineWidth=2
    ctx.beginPath(); ctx.arc(s/2,s/2,s/2-1,0,Math.PI*2); ctx.stroke()
  }
  update(player, vehicles, peds, cops){
    if(!this.ctx) return
    // center on player
    const ctx=this.ctx, s=this.size
    const px=player.pos.x, pz=player.pos.z
    const yaw=player.yaw || 0

    // we rotate map so north up, player arrow rotates
    this._drawStatic()

    // clip circle
    ctx.save()
    ctx.beginPath(); ctx.arc(s/2,s/2,s/2-2,0,Math.PI*2); ctx.clip()

    // transform: world -> minimap with rotation? keep north up, so just translate
    const worldToMap=(x,z)=>{
      return {
        x: s/2 + (x - px)*0.62,
        y: s/2 + (z - pz)*0.62
      }
    }

    // roads highlight near player — yellow center line
    ctx.strokeStyle='rgba(250,204,21,0.55)'
    ctx.lineWidth=1
    // draw nearby road segments (approx)
    for(let i=0;i<4;i++){
      const a=worldToMap(px, pz + (i-1.5)*28)
      const b=worldToMap(px+40, pz+(i-1.5)*28)
      ctx.beginPath(); ctx.moveTo(a.x,a.y); ctx.lineTo(b.x,b.y); ctx.stroke()
    }

    // mission marker — Ocean View Hotel & Banshee
    const missionTargets=[
      { x:68, z:-520, color:'#FF2E8A', icon:'♦' },
      { x:-280, z:-460, color:'#FFD600', icon:'★' },
    ]
    missionTargets.forEach(m=>{
      const p=worldToMap(m.x,m.z)
      if(Math.hypot(p.x-s/2,p.y-s/2) < s/2-6){
        ctx.fillStyle=m.color
        ctx.beginPath(); ctx.arc(p.x,p.y,5,0,Math.PI*2); ctx.fill()
        ctx.fillStyle='#000'; ctx.font='700 7px monospace'; ctx.textAlign='center'; ctx.textBaseline='middle'
        ctx.fillText(m.icon,p.x,p.y+0.5)
        // pulse
        ctx.strokeStyle=m.color; ctx.globalAlpha=0.35; ctx.lineWidth=1.5
        ctx.beginPath(); ctx.arc(p.x,p.y, 8 + Math.sin(Date.now()*0.005)*2,0,Math.PI*2); ctx.stroke(); ctx.globalAlpha=1
      } else {
        // edge indicator
        const ang=Math.atan2(m.z - pz, m.x - px)
        const ex=s/2 + Math.cos(ang)*(s/2-10), ey=s/2 + Math.sin(ang)*(s/2-10)
        ctx.fillStyle=m.color; ctx.beginPath(); ctx.arc(ex,ey,4,0,Math.PI*2); ctx.fill()
      }
    })

    // vehicles
    vehicles.forEach(v=>{
      const p=worldToMap(v.pos.x, v.pos.z)
      if(Math.hypot(p.x-s/2,p.y-s/2) > s/2-4) return
      ctx.fillStyle= v.occupied? '#00E5FF' : v.name.includes('VCPD')? '#1D4ED8' : 'rgba(255,255,255,0.9)'
      ctx.beginPath(); ctx.arc(p.x,p.y,3,0,Math.PI*2); ctx.fill()
      if(v.occupied){
        ctx.strokeStyle='#fff'; ctx.lineWidth=1; ctx.stroke()
      }
    })

    // peds
    peds.slice(0,48).forEach(p=>{
      const m=worldToMap(p.pos.x,p.pos.z)
      if(Math.hypot(m.x-s/2,m.y-s/2) > s/2-4) return
      ctx.fillStyle='rgba(255,255,255,0.55)'
      ctx.fillRect(m.x-1,m.y-1,2,2)
    })
    // cops
    cops.forEach(c=>{
      const m=worldToMap(c.pos.x,c.pos.z)
      if(Math.hypot(m.x-s/2,m.y-s/2) > s/2-4) return
      ctx.fillStyle='#FF3B30'
      ctx.beginPath(); ctx.arc(m.x,m.y,3.4,0,Math.PI*2); ctx.fill()
      ctx.fillStyle='#fff'; ctx.font='600 5px monospace'; ctx.textAlign='center'; ctx.fillText('!',m.x,m.y+0.4)
    })

    // player
    ctx.fillStyle='#fff'
    ctx.strokeStyle='rgba(0,0,0,0.6)'; ctx.lineWidth=2
    ctx.beginPath()
    // arrow pointing yaw
    const ang2=yaw
    const pr=7
    ctx.moveTo(s/2 + Math.sin(ang2)*pr, s/2 + Math.cos(ang2)*pr)
    ctx.lineTo(s/2 + Math.sin(ang2+2.6)*pr*0.7, s/2 + Math.cos(ang2+2.6)*pr*0.7)
    ctx.lineTo(s/2 + Math.sin(ang2-2.6)*pr*0.7, s/2 + Math.cos(ang2-2.6)*pr*0.7)
    ctx.closePath(); ctx.fill(); ctx.stroke()
    // inner dot
    ctx.fillStyle='#FF2E8A'; ctx.beginPath(); ctx.arc(s/2,s/2,2.2,0,Math.PI*2); ctx.fill()

    // compass N
    ctx.fillStyle='rgba(255,255,255,0.9)'
    ctx.font='700 8px JetBrains Mono'; ctx.textAlign='center'
    // north indicator at edge
    const northY= s/2 - (s/2-10)
    ctx.fillText('N', s/2, northY+3)

    ctx.restore()

    // vignette
    const grd=ctx.createRadialGradient(s/2,s/2, s/2-14, s/2,s/2, s/2)
    grd.addColorStop(0,'rgba(0,0,0,0)'); grd.addColorStop(1,'rgba(0,0,0,0.32)')
    ctx.fillStyle=grd
    ctx.beginPath(); ctx.arc(s/2,s/2,s/2,0,Math.PI*2); ctx.fill()
  }
}
