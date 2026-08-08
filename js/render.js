/* ============================================================
   GRAVITON :: render — canvas primitives, nebula, starfield,
   celestial bodies, particles, psychedelic overlays
   ============================================================ */
const Render = (() => {
  let ctx=null, W=0, H=0;

  // background objects
  let stars=[], deepStars=[], nebulaClouds=[];
  let time=0;

  function init(c){
    ctx=c;
    W=ctx.canvas.width; H=ctx.canvas.height;
    buildBackdrop();
  }
  function resize(w,h){ W=w; H=h; ctx.canvas.width=w; ctx.canvas.height=h; buildBackdrop(); }

  function buildBackdrop(){
    stars=[]; deepStars=[]; nebulaClouds=[];
    const n=Math.floor(W*H/6000);
    for(let i=0;i<n;i++) stars.push({x:Math.random()*W,y:Math.random()*H,z:Util.rand(0.2,1),tw:Math.random()*6.28});
    for(let i=0;i<160;i++) deepStars.push({x:Math.random()*W,y:Math.random()*H,r:Math.random()*1.2+0.3,ph:Math.random()*6.28});
    for(let i=0;i<14;i++) nebulaClouds.push({
      x:Math.random()*W, y:Math.random()*H, r:Util.rand(120,340),
      hue:Math.random()*360, a:Util.rand(0.04,0.10), sp:Util.rand(0.2,0.9),
    });
  }

  function frame(dt, hueShift, parallaxX, parallaxY){
    time+=dt;
    paintBackdrop(hueShift, parallaxX, parallaxY);
  }

  function paintBackdrop(hueShift, px, py){
    // deep gradient
    let g=ctx.createRadialGradient(W*0.5+px*0.02, H*0.5+py*0.02, 40, W*0.5, H*0.5, Math.max(W,H)*0.75);
    g.addColorStop(0, `hsl(${(hueShift+220)%360} 60% 7%)`);
    g.addColorStop(0.6, `hsl(${(hueShift+260)%360} 70% 3%)`);
    g.addColorStop(1, '#010005');
    ctx.fillStyle=g; ctx.fillRect(0,0,W,H);

    // nebula clouds
    for(const c of nebulaClouds){
      const h=(c.hue+hueShift)%360;
      const rg=ctx.createRadialGradient(c.x+px*c.sp*0.2, c.y+py*c.sp*0.2, 10, c.x, c.y, c.r);
      rg.addColorStop(0, `hsla(${h} 90% 55% ${c.a+0.03})`);
      rg.addColorStop(1, 'transparent');
      ctx.fillStyle=rg; ctx.beginPath(); ctx.arc(c.x,c.y,c.r,0,6.283); ctx.fill();
    }

    // deep stars
    for(const s of deepStars){
      const tw=0.5+0.5*Math.sin(time*1.4+s.ph);
      ctx.fillStyle=`hsla(${(hueShift+180)%360} 100% 80% ${0.4+0.4*tw})`;
      ctx.beginPath(); ctx.arc(s.x+px*0.05, s.y+py*0.05, s.r, 0, 6.283); ctx.fill();
    }

    // paralla star field
    for(const s of stars){
      const tw=0.5+0.5*Math.sin(time*2+s.tw);
      const size=s.z*1.6;
      ctx.fillStyle=`rgba(255,255,255,${0.35+0.5*tw*s.z})`;
      ctx.fillRect(((s.x+px*s.z)%W+W)%W, ((s.y+py*s.z)%H+H)%H, size, size);
    }
  }

  /* ---------- celestial bodies ---------- */
  function planet(b, hueShift){
    const {x,y,r}=b;
    // glow halo
    const halo=ctx.createRadialGradient(x,y,r*0.1,x,y,r*4.5);
    halo.addColorStop(0, b.glow);
    halo.addColorStop(1,'transparent');
    ctx.fillStyle=halo; ctx.beginPath(); ctx.arc(x,y,r*4.5,0,6.283); ctx.fill();

    const body=ctx.createRadialGradient(x-r*0.3,y-r*0.3,r*0.1,x,y,r);
    body.addColorStop(0,b.colorLight); body.addColorStop(1,b.color);
    ctx.fillStyle=body; ctx.beginPath(); ctx.arc(x,y,r,0,6.283); ctx.fill();

    // surface bands / swirls
    ctx.save(); ctx.beginPath(); ctx.arc(x,y,r,0,6.283); ctx.clip();
    for(const band of b.bands||[]){
      const yy=y+band* r*0.6;
      ctx.strokeStyle=`hsla(${(hueShift+band*40)%360} 80% 60% 0.35)`;
      ctx.lineWidth=Util.rand(2,6); ctx.beginPath();
      ctx.moveTo(x-r,yy); ctx.quadraticCurveTo(x, yy+8, x+r, yy); ctx.stroke();
    }
    ctx.restore();
  }

  function blackHole(b, hueShift, shipPos){
    const {x,y,r}=b;
    // gravitational lens ring — a shimmering accretion disk
    for(let i=0;i<3;i++){
      const ring=(r+6+i*14);
      const gr=ctx.createRadialGradient(x,y,ring-12,x,y,ring+12);
      gr.addColorStop(0,`hsla(${(hueShift+300)%360} 100% 60% 0.0)`);
      gr.addColorStop(0.5,`hsla(${(hueShift+320)%360} 100% 65% ${0.16})`);
      gr.addColorStop(1,'transparent');
      ctx.strokeStyle=gr; ctx.lineWidth=16; ctx.beginPath(); ctx.arc(x,y,ring,0,6.283); ctx.stroke();
    }

    // swirling accretion stream
    ctx.save();
    ctx.translate(x,y);
    for(let i=0;i<40;i++){
      const ang=i*0.4+time*1.2;
      const rr=r+6+i*3.2;
      const px=Math.cos(ang)*rr, py=Math.sin(ang)*rr;
      ctx.fillStyle=`hsla(${(hueShift+280+i*2)%360} 100% 70% ${0.05+0.05*Math.sin(time*3+i)})`;
      ctx.beginPath(); ctx.arc(px,py,1.6+Math.sin(time*4+i)*1.2,0,6.283); ctx.fill();
    }
    ctx.restore();

    // the void / event horizon
    const vg=ctx.createRadialGradient(x,y,0,x,y,r);
    vg.addColorStop(0,'#000');
    vg.addColorStop(0.85,'#000');
    vg.addColorStop(0.92,`hsl(${(hueShift+300)%360} 100% 55%)`);
    vg.addColorStop(1,'transparent');
    ctx.fillStyle=vg; ctx.beginPath(); ctx.arc(x,y,r,0,6.283); ctx.fill();

    // lensed background glimmer around horizon (bending light)
    const lg=ctx.createRadialGradient(x,y,r*0.9,x,y,r*1.5);
    lg.addColorStop(0,'transparent');
    lg.addColorStop(0.5,`hsla(${(hueShift+100)%360} 90% 70% 0.10)`);
    lg.addColorStop(1,'transparent');
    ctx.fillStyle=lg; ctx.beginPath(); ctx.arc(x,y,r*1.5,0,6.283); ctx.fill();

    // gravitational lensing distortion of nearby stars: draw arc smears
    for(let i=0;i<6;i++){
      const ang=time*0.3+i*1.05;
      const lr=r*1.35;
      ctx.strokeStyle=`hsla(${(hueShift+i*50)%360} 100% 80% 0.10)`;
      ctx.lineWidth=1.5; ctx.beginPath();
      ctx.arc(x,y,lr,ang,ang+0.7); ctx.stroke();
    }
  }

  /* ---------- shards ---------- */
  function shard(s, hueShift){
    const {x,y,r}=s;
    const t=time*3+s.ph;
    const pulse=0.7+0.3*Math.sin(t);
    ctx.save(); ctx.translate(x,y); ctx.rotate(t*0.7);
    ctx.fillStyle=`hsla(${(s.hue+hueShift)%360} 100% 70% ${pulse})`;
    // prism shape
    ctx.beginPath();
    for(let i=0;i<4;i++){ const a=i*1.5708; const rr=r*(i%2?0.5:1); ctx.lineTo(Math.cos(a)*rr,Math.sin(a)*rr); }
    ctx.closePath(); ctx.fill();
    ctx.restore();
    const glow=ctx.createRadialGradient(x,y,0,x,y,r*3);
    glow.addColorStop(0,`hsla(${(s.hue+hueShift)%360} 100% 60% 0.35)`);
    glow.addColorStop(1,'transparent');
    ctx.fillStyle=glow; ctx.beginPath(); ctx.arc(x,y,r*3,0,6.283); ctx.fill();
  }

  /* ---------- gate (jump point) ---------- */
  function gate(g, hueShift, active){
    const {x,y,r}=g;
    const pulse=active? (1+0.06*Math.sin(time*6)) : 1;
    for(let i=0;i<8;i++){
      const rr=r*(1-i*0.1)*pulse;
      ctx.strokeStyle=`hsla(${(hueShift+i*40)%360} 100% 65% ${0.5-i*0.05})`;
      ctx.lineWidth=3;
      ctx.beginPath();
      for(let j=0;j<24;j++){ const a=j/24*6.283+time*0.3; const xx=x+Math.cos(a)*rr, yy=y+Math.sin(a)*rr*0.6; j?ctx.lineTo(xx,yy):ctx.moveTo(xx,yy); }
      ctx.stroke();
    }
    const core=ctx.createRadialGradient(x,y,0,x,y,r*0.5);
    core.addColorStop(0, active?`hsla(${(hueShift+180)%360} 100% 80% 0.8)`: `rgba(255,255,255,0.25)`);
    core.addColorStop(1,'transparent');
    ctx.fillStyle=core; ctx.beginPath(); ctx.arc(x,y,r*0.5,0,6.283); ctx.fill();
    if(active){
      ctx.strokeStyle=`hsl(${(hueShift+180)%360} 100% 70%)`;
      ctx.lineWidth=2; ctx.setLineDash([6,8]); ctx.lineDashOffset=-time*30;
      ctx.beginPath(); ctx.arc(x,y,r*0.55,0,6.283); ctx.stroke(); ctx.setLineDash([]);
    }
  }

  /* ---------- ship ---------- */
  function ship(p, hueShift){
    const {x,y,ang}=p;
    ctx.save(); ctx.translate(x,y); ctx.rotate(ang);
    // engine flame
    const fl=0.5+0.5*Math.sin(time*30);
    const flameLen=(14+16*fl)+(p.boosting?14:0);
    const fg=ctx.createRadialGradient(-4,0,-6,-14,0,flameLen);
    fg.addColorStop(0,`hsl(${(hueShift+40)%360} 100% 70%)`);
    fg.addColorStop(1,'transparent');
    ctx.fillStyle=fg; ctx.beginPath();
    ctx.moveTo(-6,-2); ctx.lineTo(-6-flameLen,0); ctx.lineTo(-6,2); ctx.closePath(); ctx.fill();
    // hull
    const hull=ctx.createLinearGradient(0,-10,0,12);
    hull.addColorStop(0,`hsl(${(hueShift+180)%360} 90% 80%)`);
    hull.addColorStop(1,`hsl(${(hueShift+240)%360} 80% 45%)`);
    ctx.fillStyle=hull;
    ctx.beginPath(); ctx.moveTo(16,0); ctx.lineTo(-8,-9); ctx.lineTo(-4,0); ctx.lineTo(-8,9); ctx.closePath(); ctx.fill();
    ctx.strokeStyle='rgba(255,255,255,0.7)'; ctx.lineWidth=1; ctx.stroke();
    // cockpit light
    ctx.fillStyle='rgba(255,255,255,0.9)';
    ctx.beginPath(); ctx.arc(6,0,2.4,0,6.283); ctx.fill();
    ctx.restore();
    // ship glow
    const sg=ctx.createRadialGradient(x,y,0,x,y,40);
    sg.addColorStop(0,`hsla(${(hueShift+180)%360} 100% 70% 0.18)`);
    sg.addColorStop(1,'transparent');
    ctx.fillStyle=sg; ctx.beginPath(); ctx.arc(x,y,40,0,6.283); ctx.fill();
  }

  /* ---------- particles ---------- */
  function particles(list, hueShift, cam){
    for(const p of list){
      p.life-=p.decay;
      if(p.life<=0) continue;
      p.x+=p.vx*0.016; p.y+=p.vy*0.016;
      const sx=p.x-cam.cx+innerWidth/2, sy=p.y-cam.cy+innerHeight/2;
      ctx.fillStyle=`hsla(${p.hue} 100% ${p.light}% ${p.life*p.a})`;
      ctx.beginPath(); ctx.arc(sx,sy,p.size,0,6.283); ctx.fill();
    }
  }

  /* ---------- full-screen psychedelic pulse near horizons ---------- */
  function vignette(intensity){
    const vg=ctx.createRadialGradient(W/2,H/2,Math.min(W,H)*0.25,W/2,H/2,Math.max(W,H)*0.72);
    vg.addColorStop(0,'transparent');
    vg.addColorStop(1,`rgba(${intensity? '255,61,240':'0,0,0'},${0.5+intensity*0.2})`);
    ctx.fillStyle=vg; ctx.fillRect(0,0,W,H);
  }

  function flash(color, alpha){
    ctx.fillStyle=`hsla(${color} 100% 60% ${alpha})`;
    ctx.fillRect(0,0,W,H);
  }

  /* gravity line helpers */
  function gravityLine(x1,y1,x2,y2,hue,a){
    ctx.strokeStyle=`hsla(${hue} 90% 70% ${a})`;
    ctx.lineWidth=1; ctx.beginPath(); ctx.moveTo(x1,y1); ctx.lineTo(x2,y2); ctx.stroke();
  }

  return { init, resize, frame, planet, blackHole, shard, gate, ship, particles, vignette, flash, gravityLine };
})();
