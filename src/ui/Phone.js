// AGENT 19 — VINEWOOD — Phone, Missions, Dialog
export class Phone {
  constructor(){
    this.el=document.getElementById('phone')
    this.titleEl=document.getElementById('mission-title')
    this.descEl=document.getElementById('mission-desc')
    this.fillEl=document.getElementById('mission-fill')
    this.visible=false
    this.autoHide=null
  }
  show(title, desc, progress=0.35){
    if(this.titleEl) this.titleEl.textContent=title
    if(this.descEl) this.descEl.innerHTML=desc
    if(this.fillEl) this.fillEl.style.width=`${Math.round(progress*100)}%`
    this.el?.classList.add('show')
    this.visible=true
    clearTimeout(this.autoHide)
    // auto hide after 7s if not mission
    if(progress<1) this.autoHide=setTimeout(()=>this.hide(), 7200)
  }
  hide(){
    this.el?.classList.remove('show')
    this.visible=false
  }
  toggle(){
    if(this.visible) this.hide()
    else this.show('PHONE — Contacts', 'Tap a contact to start a mission.<br><br><b style="color:#fff">• Lucia — Ocean View</b> (Active)<br>• Jason — Marina Run<br>• VCPD — Dispatch', 0.5)
  }
}
