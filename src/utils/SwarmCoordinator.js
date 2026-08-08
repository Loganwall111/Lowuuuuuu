// AGENT 20 — OVERLORD / Swarm Coordinator
// Orchestrates 20 agents in 4 phases, seamless, automatic

export class SwarmCoordinator {
  constructor() {
    this.agents = [
      { id: 1, code: 'NEXUS', phase: 1, task: 'Core Engine & Renderer' },
      { id: 2, code: 'SYNAPSE', phase: 1, task: 'Input & Controls' },
      { id: 3, code: 'NEWTON', phase: 1, task: 'Physics & Collision' },
      { id: 4, code: 'ECHO', phase: 1, task: 'Audio & Radio' },
      { id: 5, code: 'ATLAS', phase: 2, task: 'City Layout & Districts' },
      { id: 6, code: 'MERCATOR', phase: 2, task: 'Road Network & Graph' },
      { id: 7, code: 'MONOLITH', phase: 2, task: 'Buildings & Interiors' },
      { id: 8, code: 'EDEN', phase: 2, task: 'Vegetation & Props' },
      { id: 9, code: 'POSEIDON', phase: 2, task: 'Ocean & Beach Shader' },
      { id: 10, code: 'CHRONOS', phase: 2, task: 'Sky, Weather & Time' },
      { id: 11, code: 'LUCIA', phase: 3, task: 'Player Controller' },
      { id: 12, code: 'CROWD', phase: 3, task: 'Pedestrian AI' },
      { id: 13, code: 'BADGE', phase: 3, task: 'Police & Wanted' },
      { id: 14, code: 'ARSENAL', phase: 3, task: 'Weapons & Combat' },
      { id: 15, code: 'TORQUE', phase: 3, task: 'Vehicle Physics' },
      { id: 16, code: 'FLOW', phase: 3, task: 'Traffic Simulation' },
      { id: 17, code: 'HUDSON', phase: 4, task: 'HUD & Effects' },
      { id: 18, code: 'CARTO', phase: 4, task: 'Minimap & Radar' },
      { id: 19, code: 'VINEWOOD', phase: 4, task: 'Missions & Phone' },
      { id: 20, code: 'OVERLORD', phase: 4, task: 'QA & Polish' },
    ]
    this.phase = 0
    this.done = new Set()
    this.statusEl = document.getElementById('loader-status')
    this.barEl = document.getElementById('loader-bar')
    this.agentsEl = document.getElementById('loader-agents')
    this._buildDots()
  }
  _buildDots(){
    if(!this.agentsEl) return
    this.agentsEl.innerHTML = ''
    this.agents.forEach(a=>{
      const d=document.createElement('div')
      d.className='agent-dot'
      d.id=`agent-${a.id}`
      d.textContent=String(a.id).padStart(2,'0')
      d.title=`${a.code} — ${a.task}`
      this.agentsEl.appendChild(d)
    })
  }
  _setActive(id){
    document.querySelectorAll('.agent-dot').forEach(e=>e.classList.remove('active'))
    const el=document.getElementById(`agent-${id}`)
    if(el) el.classList.add('active')
  }
  _setDone(id){
    const el=document.getElementById(`agent-${id}`)
    if(el){ el.classList.remove('active'); el.classList.add('done') }
    this.done.add(id)
  }
  async phaseLog(name){
    this.phase++
    console.log(`%c◼ PHASE ${this.phase}: ${name}`, 'background:#ff2e8a;color:#fff;padding:3px 8px;border-radius:6px;font-weight:800')
  }
  async runAgent(id, fn){
    const agent=this.agents.find(a=>a.id===id)
    this._setActive(id)
    if(this.statusEl) this.statusEl.textContent=`AGENT ${String(id).padStart(2,'0')} — ${agent.code} — ${agent.task.toUpperCase()}…`
    const t0=performance.now()
    try{ await fn() }catch(e){ console.error(`Agent ${id} ${agent.code} failed`, e) }
    const dt=Math.round(performance.now()-t0)
    console.log(`✔ Agent ${String(id).padStart(2,'0')} ${agent.code} — ${agent.task} (${dt}ms)`)
    this._setDone(id)
    if(this.barEl) this.barEl.style.width=`${Math.round((this.done.size/20)*100)}%`
    // tiny breathe for visual
    await new Promise(r=>setTimeout(r, 80 + Math.random()*90))
  }
  async complete(){
    if(this.statusEl) this.statusEl.textContent='ALL 20 AGENTS COMPLETE — POLISHING VICE CITY…'
    if(this.barEl) this.barEl.style.width='100%'
    await new Promise(r=>setTimeout(r, 420))
    const loader=document.getElementById('loader')
    if(loader) loader.classList.add('hidden')
    const start=document.getElementById('start')
    if(start) start.classList.remove('hidden')
    console.log('%c✦ GTA VI LEONIDA — SWARM BUILD COMPLETE — ABSOLUTELY STUNNING ✦', 'background:linear-gradient(90deg,#ff2e8a,#00e5ff);color:#fff;padding:6px 12px;border-radius:8px;font-size:14px;font-weight:900')
  }
}
