// VINEWOOD — Missions
import * as THREE from 'three'

export class MissionManager {
  constructor(phone, hud, police){
    this.phone=phone; this.hud=hud; this.police=police
    this.current=null
    this.stage=0
    this.markers=[]
    this.scene=null
  }
  init(scene){
    this.scene=scene
    this.startMission('LUCIA_CALL')
  }
  startMission(id){
    if(id==='LUCIA_CALL'){
      this.current={ id, title:"LUCIA'S CALL", desc: "Grab the <b style='color:#fff'>Banshee</b> at the marina, shake any heat, and bring it to the <b style='color:#FFD600'>Ocean View Hotel</b>." }
      this.stage=0
      this.phone.show(this.current.title, this.current.desc, 0.18)
      this._setMarker(new THREE.Vector3(68,-0.5,-520), 0xFF2E8A, 'BANSHEE')
      this._toast('Mission started: Lucia\'s Call')
    }
  }
  _setMarker(pos, color, label){
    this.clearMarkers()
    const geo=new THREE.CylinderGeometry(2.2,2.2,0.12,22)
    const mat=new THREE.MeshStandardMaterial({ color, emissive:color, emissiveIntensity:1.1, transparent:true, opacity:0.92 })
    const ring=new THREE.Mesh(geo, mat)
    ring.position.copy(pos); ring.position.y=0.12
    ring.userData={ baseY:0.12, label }
    this.scene.add(ring)
    // beam
    const beamGeo=new THREE.CylinderGeometry(0.42,1.8,46,12,1,true)
    const beamMat=new THREE.MeshBasicMaterial({ color, transparent:true, opacity:0.18, side:THREE.DoubleSide, depthWrite:false })
    const beam=new THREE.Mesh(beamGeo, beamMat)
    beam.position.copy(pos); beam.position.y=23
    this.scene.add(beam)
    // label sprite
    const canvas=document.createElement('canvas'); canvas.width=256; canvas.height=64
    const ctx=canvas.getContext('2d')
    ctx.fillStyle='rgba(10,14,28,0.92)'; ctx.strokeStyle=`#${color.toString(16).padStart(6,'0')}`; ctx.lineWidth=2
    const r=12; ctx.beginPath(); ctx.roundRect(4,4,248,56,r); ctx.fill(); ctx.stroke()
    ctx.fillStyle='#fff'; ctx.font='800 22px Bebas Neue, sans-serif'; ctx.textAlign='center'; ctx.fillText(label,128,38)
    const tex=new THREE.CanvasTexture(canvas); tex.colorSpace=THREE.SRGBColorSpace
    const sprMat=new THREE.SpriteMaterial({ map:tex, transparent:true })
    const sprite=new THREE.Sprite(sprMat)
    sprite.position.copy(pos); sprite.position.y=8.5; sprite.scale.set(18,4.5,1)
    this.scene.add(sprite)

    this.markers=[ring, beam, sprite]
    this.markerPos=pos
  }
  clearMarkers(){
    this.markers.forEach(m=> this.scene?.remove(m))
    this.markers=[]
  }
  update(player){
    if(!this.current || !this.markerPos) return
    // float animation
    const t=Date.now()*0.001
    this.markers.forEach(m=>{
      if(m.userData?.baseY!==undefined){
        m.position.y=m.userData.baseY + Math.sin(t*1.8)*0.22
        m.rotation.y+=0.014
      }
      if(m.isSprite){ m.position.y=8.5 + Math.sin(t*1.8)*0.18 }
    })

    const dist=player.pos.distanceTo(this.markerPos)

    if(this.stage===0){
      // need to be in Banshee and reach marker
      const nearBanshee=dist<6 && player.isInVehicle && player.currentVehicle?.name.includes('Banshee')
      if(nearBanshee || (dist<3 && !player.isInVehicle)){
        // if on foot near banshee, prompt enter
        if(!player.isInVehicle){
          this.phone.show('GET IN', 'Press <b style="color:#00E5FF">E</b> to enter the Banshee. Lose the cops if they spot you.', 0.42)
          return
        }
        this.stage=1
        this._setMarker(new THREE.Vector3(-280,0.12,-460), 0xFFD600, 'OCEAN VIEW')
        this.phone.show('GO GO GO!', 'Nice! Now bring it to the <b style="color:#FFD600">Ocean View Hotel</b>. VCPD is on alert — don\'t get boxed in!', 0.58)
        this.hud?.addMoney(0) // trigger
        this._toast('Banshee secured — Move to Ocean View!')
        // add wanted to spice
        if(Math.random()>0.4) this.police?.setWanted(1)
      }
    } else if(this.stage===1){
      if(dist<10 && player.isInVehicle){
        this.stage=2
        this.clearMarkers()
        this.phone.show('MISSION PASSED!', '<b style="color:#00E5FF">+$2,500</b> • Respect +<br>Lucia is waiting inside. Take a breath — Vice City is yours.', 1.0)
        this.hud?.addMoney(2500)
        this.police?.setWanted(0)
        this._toast('★ MISSION PASSED — LUCIA\'S CALL ★')
        // fireworks
        this._fireworks(player.pos)
        setTimeout(()=>{
          this.phone.show('FREE ROAM', 'Keep exploring — try the causeway at sunset, hit 120 MPH, and switch radio with <b style="color:#FF2E8A">Q</b>. New missions coming soon.', 0.72)
        }, 4200)
      }
    }
  }
  _toast(msg){
    const t=document.getElementById('toast')
    if(!t) return
    t.textContent=msg; t.classList.add('show'); setTimeout(()=>t.classList.remove('show'), 3400)
  }
  _fireworks(pos){
    for(let i=0;i<5;i++){
      setTimeout(()=>{
        const c=new THREE.PointLight([0xFF2E8A,0x00E5FF,0xFFD600,0x7C4DFF][i%4], 1800, 90)
        c.position.copy(pos).add(new THREE.Vector3((Math.random()-0.5)*18, 12+Math.random()*14, (Math.random()-0.5)*18))
        this.scene.add(c)
        setTimeout(()=>this.scene.remove(c), 600)
      }, i*220)
    }
  }
}
