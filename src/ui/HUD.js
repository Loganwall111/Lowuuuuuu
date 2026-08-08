// AGENT 17 — HUDSON — HUD, Effects
export class HUD {
  constructor(){
    this.moneyEl=document.getElementById('hud-money')
    this.healthEl=document.getElementById('bar-health')
    this.armorEl=document.getElementById('bar-armor')
    this.staminaEl=document.getElementById('bar-stamina')
    this.money=8472
  }
  addMoney(delta){
    this.money+=delta
    if(this.moneyEl) this.moneyEl.innerHTML=`<span>$</span> ${this.money.toLocaleString()}`
  }
  shake(intensity=1){
    document.body.animate([
      { transform:`translate(${intensity*3}px, ${intensity*2}px)` },
      { transform:`translate(${-intensity*3}px, ${-intensity*1}px)` },
      { transform:`translate(0,0)` }
    ], { duration: 180, easing:'ease-out' })
  }
  hitmarker(){
    this.shake(0.7)
  }
}
