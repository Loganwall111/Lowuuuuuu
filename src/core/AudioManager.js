// AGENT 04 — ECHO — Audio Manager (WebAudio, procedural radio)
export class AudioManager {
  constructor(){
    this.ctx=null
    this.gainMaster=null
    this.radioIndex=0
    this.stations=[]
    this.enabled=false
    this.sounds=new Map()
  }
  async init(){
    try{
      this.ctx = new (window.AudioContext || window.webkitAudioContext)()
      this.gainMaster = this.ctx.createGain()
      this.gainMaster.gain.value=0.82
      this.gainMaster.connect(this.ctx.destination)
      this.enabled=true
    }catch(e){ console.warn('Audio init failed',e) }
  }
  resume(){ if(this.ctx && this.ctx.state==='suspended') this.ctx.resume() }
  // procedural SFX via oscillator
  blip(freq=440, dur=0.12, type='sine', gain=0.25){
    if(!this.enabled || !this.ctx) return
    const o=this.ctx.createOscillator(), g=this.ctx.createGain()
    o.type=type; o.frequency.value=freq
    g.gain.value=gain
    g.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime+dur)
    o.connect(g); g.connect(this.gainMaster)
    o.start(); o.stop(this.ctx.currentTime+dur)
  }
  engineSound(speed01){
    if(!this.enabled) return
    // called per frame if needed — we generate a subtle hum
  }
  switchRadio(stations){
    this.stations=stations
    this.radioIndex=(this.radioIndex+1)%stations.length
    this.blip(880,0.18,'square',0.18)
    setTimeout(()=>this.blip(1320,0.12,'sine',0.15),120)
    return this.stations[this.radioIndex]
  }
  shot(){ this.blip(180,0.08,'square',0.4); setTimeout(()=>this.blip(60,0.12,'sawtooth',0.25),30) }
  siren(t){
    if(!this.enabled) return
    const o=this.ctx.createOscillator(), g=this.ctx.createGain()
    o.type='sine'
    o.frequency.value= 650 + Math.sin(t*6)*250
    g.gain.value=0.12
    o.connect(g); g.connect(this.gainMaster)
    o.start(); o.stop(this.ctx.currentTime+0.18)
  }
}
