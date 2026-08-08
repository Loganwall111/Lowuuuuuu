// AGENT 10 — CHRONOS — Sky, Weather, Time, Volumetrics
import * as THREE from 'three'

export class Weather {
  constructor(scene, engine){
    this.scene=scene
    this.engine=engine
    this.timeOfDay=21.7 // 0-24, start night neon
    this.timeScale=0.015 // real seconds -> ingame hours
    this.rainIntensity=0
    this.targetRain=0
    this.lightningTimer=0
  }
  build(){
    // Sky dome — gradient shader
    const skyGeo=new THREE.SphereGeometry(2800, 32, 16)
    const skyMat=new THREE.ShaderMaterial({
      uniforms:{
        uTop:{value:new THREE.Color(0x0a0f1e)},
        uMid:{value:new THREE.Color(0xFF2E8A)},
        uBottom:{value:new THREE.Color(0xFFB86B)},
        uSunPos:{value:new THREE.Vector3(0.5,0.35,-0.6)},
        uTime:{value:0}
      },
      vertexShader:`varying vec3 vPos; void main(){ vPos=position; gl_Position=projectionMatrix*modelViewMatrix*vec4(position,1.0); }`,
      fragmentShader:`
        varying vec3 vPos;
        uniform vec3 uTop,uMid,uBottom;
        uniform vec3 uSunPos;
        uniform float uTime;
        void main(){
          float h = normalize(vPos).y;
          float t = clamp(h*0.5+0.5,0.0,1.0);
          vec3 col = mix(uBottom, uMid, smoothstep(0.0,0.45,t));
          col = mix(col, uTop, smoothstep(0.45,1.0,t));
          // sun glow
          float sun = pow(max(0.0, dot(normalize(vPos), normalize(uSunPos))), 32.0);
          col += sun * vec3(1.0,0.92,0.7)*0.9;
          // horizon haze
          float haze = pow(max(0.0, 1.0 - abs(h)*1.8), 2.0)*0.25;
          col += haze*vec3(1.0,0.6,0.85);
          // stars at night
          float night = smoothstep(0.2,0.0, h);
          float stars = fract(sin(dot(vPos.xz, vec2(12.9898,78.233)))*43758.5453);
          stars = step(0.995, stars)* night * 0.5;
          col += stars;
          gl_FragColor=vec4(col,1.0);
        }
      `,
      side:THREE.BackSide,
      fog:false
    })
    this.sky=new THREE.Mesh(skyGeo, skyMat)
    this.scene.add(this.sky)
    this.skyMat=skyMat

    // Clouds — soft planes
    this.clouds=new THREE.Group()
    for(let i=0;i<18;i++){
      const g=new THREE.PlaneGeometry(900+Math.random()*900, 220+Math.random()*180)
      const m=new THREE.MeshBasicMaterial({ color:0xffffff, transparent:true, opacity:0.08+Math.random()*0.08, depthWrite:false, side:THREE.DoubleSide })
      const mesh=new THREE.Mesh(g,m)
      const a=Math.random()*Math.PI*2
      const r=900+Math.random()*900
      mesh.position.set(Math.cos(a)*r, 420+Math.random()*120, Math.sin(a)*r)
      mesh.lookAt(0,500,0)
      mesh.userData={ baseY:mesh.position.y, speed: 0.15+Math.random()*0.35 }
      this.clouds.add(mesh)
    }
    this.scene.add(this.clouds)

    // Rain particles
    const rainGeo=new THREE.BufferGeometry()
    const count=3800
    const pos=new Float32Array(count*3)
    for(let i=0;i<count;i++){
      pos[i*3]=(Math.random()-0.5)*2200
      pos[i*3+1]=Math.random()*900
      pos[i*3+2]=(Math.random()-0.5)*2200
    }
    rainGeo.setAttribute('position', new THREE.BufferAttribute(pos,3))
    const rainMat=new THREE.PointsMaterial({ color:0x9ecfff, size:1.9, transparent:true, opacity:0, depthWrite:false })
    this.rain=new THREE.Points(rainGeo,rainMat)
    this.scene.add(this.rain)
  }
  update(dt, elapsed){
    this.timeOfDay = (this.timeOfDay + dt*this.timeScale)%24
    // sky color by time
    const isDay = this.timeOfDay>6 && this.timeOfDay<19.5
    const t = this.timeOfDay/24
    // sun position
    const sunAngle = (t*2*Math.PI - Math.PI/2)
    const sunPos=new THREE.Vector3(Math.cos(sunAngle)*2200, Math.sin(sunAngle)*1400+300, -600)
    if(this.engine?.sun){
      this.engine.sun.position.copy(sunPos)
      this.engine.sun.intensity = isDay? 2.9 : 0.22
      this.engine.sun.color.setHSL(isDay?0.12:0.62, isDay?0.35:0.45, isDay?1:0.85)
    }
    if(this.engine?.hemi){
      this.engine.hemi.intensity = isDay?0.95:0.42
    }
    if(this.skyMat){
      this.skyMat.uniforms.uSunPos.value.copy(sunPos.clone().normalize())
      this.skyMat.uniforms.uTime.value=elapsed
      if(isDay){
        this.skyMat.uniforms.uTop.value.setHex(0x1e3a8a)
        this.skyMat.uniforms.uMid.value.setHex(0x60a5fa)
        this.skyMat.uniforms.uBottom.value.setHex(0x93c5fd)
      } else {
        // neon sunset night
        const sunset = (this.timeOfDay>17 && this.timeOfDay<20.5)
        if(sunset){
          this.skyMat.uniforms.uTop.value.setHex(0x0a1020)
          this.skyMat.uniforms.uMid.value.set(0xff2e8a)
          this.skyMat.uniforms.uBottom.value.set(0xffb86b)
        } else {
          this.skyMat.uniforms.uTop.value.setHex(0x070a18)
          this.skyMat.uniforms.uMid.value.setHex(0x1a0b2e)
          this.skyMat.uniforms.uBottom.value.setHex(0x2a0f3a)
        }
      }
    }
    // rain control — random storms
    if(Math.random()<0.002) this.targetRain = Math.random()>0.65? 0.7+Math.random()*0.6 : 0
    this.rainIntensity += (this.targetRain - this.rainIntensity)*dt*0.4
    if(this.rain){
      this.rain.material.opacity = this.rainIntensity*0.55
      const p=this.rain.geometry.attributes.position
      for(let i=0;i<p.count;i++){
        let y=p.getY(i)
        y -= dt* 420 * (0.6+this.rainIntensity)
        if(y<0){ y=900+Math.random()*80; p.setX(i,(Math.random()-0.5)*2200); p.setZ(i,(Math.random()-0.5)*2200) }
        p.setY(i,y)
      }
      p.needsUpdate=true
    }
    // clouds drift
    if(this.clouds){
      this.clouds.children.forEach(c=>{
        c.position.x += dt*c.userData.speed*12
        if(c.position.x>1800) c.position.x=-1800
      })
    }
    // lightning
    if(this.rainIntensity>0.5 && Math.random()<0.008){
      this._flash()
    }
  }
  _flash(){
    if(!this.engine?.scene) return
    const flash=new THREE.PointLight(0xC8E6FF, 9000, 3000)
    flash.position.set((Math.random()-0.5)*1200, 380, (Math.random()-0.5)*1200)
    this.scene.add(flash)
    setTimeout(()=>this.scene.remove(flash), 120)
    setTimeout(()=>{
      const flash2=flash.clone()
      this.scene.add(flash2)
      setTimeout(()=>this.scene.remove(flash2), 80)
    }, 180)
  }
}
