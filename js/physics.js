/* ============================================================
   GRAVITON :: physics — Newtonian gravity, ship integration,
   black holes, collisions, particles
   ============================================================ */
const Physics = (() => {

  // A body: planet or black hole
  function makeBody(type, x, y, mass, r, palette){
    const hue = Util.randInt(0,359);
    return {
      type, x, y, mass, r,
      hue,
      color:`hsl(${hue} 70% 45%)`,
      colorLight:`hsl(${(hue+20)%360} 90% 68%)`,
      glow:`hsla(${hue} 90% 55% 0.28)`,
      bands: type==='planet' ? [Util.rand(-1,1),Util.rand(-1,1),Util.rand(-1,1)] : [],
    };
  }

  // Apply gravitational acceleration to pos object (vx,vy). Returns feel intensity.
  function gravityForces(bodies, x, y, vx, vy, dt, out){
    let gx=0, gy=0;
    for(const b of bodies){
      const dx=b.x-x, dy=b.y-y;
      const d2=dx*dx+dy*dy;
      const d=Math.sqrt(d2)+8;
      const f=CONFIG.gravity*b.mass/d2;
      gx+=dx/d*f; gy+=dy/d*f;
    }
    out.vx = vx+gx*dt; out.vy = vy+gy*dt;
    return {gx,gy};
  }

  // Ship update with thrust + gravity + drag. Returns state flags.
  function updateShip(p, bodies, input, dt, out){
    // input thrust
    let ax=0, ay=0;
    const thrust=(input.up?1:0)-(input.down?1:0);
    const strafe=(input.right?1:0)-(input.left?1:0);
    const cos=Math.cos(p.ang), sin=Math.sin(p.ang);
    // forward thrust along ship angle
    if(input.forward){
      ax += cos*CONFIG.shipAccel*(p.boosting?2.6:1);
      ay += sin*CONFIG.shipAccel*(p.boosting?2.6:1);
    }
    // strafe
    ax += -sin*strafe*CONFIG.shipAccel;
    ay += cos*strafe*CONFIG.shipAccel;
    // vertical
    ax += cos*thrust*0*0; // (not used; up/down = strafe)

    // gravity
    const g = gravityForces(bodies, p.x, p.y, p.vx+ax*dt, p.vy+ay*dt, dt, out);
    // drag
    const d=Math.pow(CONFIG.drag, dt*60);
    out.vx*=d; out.vy*=d;
    // clamp speed
    const sp=Math.hypot(out.vx,out.vy);
    const max=CONFIG.shipMaxSpeed*(p.boosting?1.6:1);
    if(sp>max){ out.vx*=max/sp; out.vy*=max/sp; }
    p.vx=out.vx; p.vy=out.vy;
    p.x+=p.vx*dt; p.y+=p.vy*dt;
    return g;
  }

  // Update ship angle toward mouse/facing direction (auto-aim) or manual
  function steerShip(p, targetAng, dt, turnRate){
    // shortest-angle turn
    let d=((targetAng-p.ang+Math.PI*3)%(Math.PI*2))-Math.PI;
    const maxTurn=turnRate*dt;
    d=Util.clamp(d,-maxTurn,maxTurn);
    p.ang+=d;
  }

  // Find nearest gravitational "feel" near the ship for visuals
  function nearBody(bodies, x, y){
    let best=null, bd=1e18;
    for(const b of bodies){
      const d=Util.dist(x,y,b.x,b.y);
      if(d<b.r*8 && d<bd){ bd=d; best=b; }
    }
    return {body:best, dist:bd};
  }

  /* ---------- particles ---------- */
  function emit(particles, hueShift, x,y,vx,vy, n, speed, size, light){
    for(let i=0;i<n;i++){
      const a=Math.random()*6.283, s=speed*(0.4+Math.random()*0.9);
      particles.push({
        x,y,
        vx:vx+Math.cos(a)*s, vy:vy+Math.sin(a)*s,
        life:1, decay:Util.rand(0.6,1.5),
        size:Util.rand(1,size), hue:(hueShift+Util.randInt(0,80))%360, light:light||70, a:0.8,
      });
    }
  }

  /* ---------- camera ---------- */
  function camera(ship, target, W, H, dt){
    const k=3.5*dt;
    target.cx=Util.lerp(target.cx, ship.x, k);
    target.cy=Util.lerp(target.cy, ship.y, k);
    // shake
    if(target.shake>0.2){ target.cx+=Util.rand(-target.shake,target.shake); target.cy+=Util.rand(-target.shake,target.shake); }
    target.shake*=Math.pow(0.001,dt);
  }
  function toScreen(x,y,cam){ return {x:x-cam.cx+innerWidth/2, y:y-cam.cy+innerHeight/2}; }
  function toWorld(sx,sy,cam){ return {x:sx+ cam.cx-innerWidth/2, y:sy+cam.cy-innerHeight/2}; }

  return { makeBody, updateShip, steerShip, nearBody, emit, camera, toScreen, toWorld, gravityForces };
})();
