// AGENT 02 — SYNAPSE — Input System
export class Input {
  constructor(domElement){
    this.dom = domElement || document.body
    this.keys = new Set()
    this.mouse = { x:0, y:0, dx:0, dy:0, left:false, right:false, wheel:0 }
    this.pointerLocked = false
    this._down = this._down.bind(this)
    this._up = this._up.bind(this)
    this._move = this._move.bind(this)
    this._wheel = this._wheel.bind(this)
    this._pointerLockChange = this._pointerLockChange.bind(this)
    this._mouseDown = this._mouseDown.bind(this)
    this._mouseUp = this._mouseUp.bind(this)
    this._ctx = this._ctx.bind(this)

    window.addEventListener('keydown', this._down)
    window.addEventListener('keyup', this._up)
    window.addEventListener('mousemove', this._move)
    window.addEventListener('wheel', this._wheel, { passive:true })
    window.addEventListener('mousedown', this._mouseDown)
    window.addEventListener('mouseup', this._mouseUp)
    document.addEventListener('pointerlockchange', this._pointerLockChange)
    window.addEventListener('contextmenu', this._ctx)

    // touch
    this.touch = { active:false, x:0, y:0, dx:0, dy:0 }
    window.addEventListener('touchstart', e=>{ this.touch.active=true }, {passive:true})
    window.addEventListener('touchend', e=>{ this.touch.active=false }, {passive:true})
  }
  _ctx(e){ if(this.pointerLocked) e.preventDefault() }
  _down(e){
    this.keys.add(e.code)
    if(['Space','ArrowUp','ArrowDown','ArrowLeft','ArrowRight'].includes(e.code)) e.preventDefault()
  }
  _up(e){ this.keys.delete(e.code) }
  _move(e){
    if(this.pointerLocked){
      this.mouse.dx += e.movementX
      this.mouse.dy += e.movementY
    } else {
      this.mouse.x = (e.clientX / window.innerWidth)*2 -1
      this.mouse.y = -(e.clientY / window.innerHeight)*2 +1
    }
  }
  _wheel(e){ this.mouse.wheel = e.deltaY }
  _mouseDown(e){
    if(e.button===0) this.mouse.left=true
    if(e.button===2) this.mouse.right=true
  }
  _mouseUp(e){
    if(e.button===0) this.mouse.left=false
    if(e.button===2) this.mouse.right=false
  }
  _pointerLockChange(){
    this.pointerLocked = document.pointerLockElement === this.dom
  }
  lock(){ this.dom.requestPointerLock?.() }
  unlock(){ document.exitPointerLock?.() }
  isDown(code){ return this.keys.has(code) }
  consumeWheel(){ const v=this.mouse.wheel; this.mouse.wheel=0; return v }
  consumeMouseDelta(){
    const dx=this.mouse.dx, dy=this.mouse.dy
    this.mouse.dx=0; this.mouse.dy=0
    return {dx,dy}
  }
  dispose(){
    window.removeEventListener('keydown', this._down)
    window.removeEventListener('keyup', this._up)
    window.removeEventListener('mousemove', this._move)
    window.removeEventListener('wheel', this._wheel)
  }
}
