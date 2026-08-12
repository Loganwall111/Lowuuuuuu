// AGENT 01 — NEXUS — Core Engine, Renderer, Scene, PostFX
import * as THREE from 'three'
import { CONFIG } from '../config.js'

export class Engine {
  constructor() {
    this.scene = new THREE.Scene()
    this.scene.fog = new THREE.Fog(0x0b1426, CONFIG.render.fogNear, CONFIG.render.fogFar)
    this.scene.background = new THREE.Color(0x070a12)

    this.camera = new THREE.PerspectiveCamera(72, window.innerWidth/window.innerHeight, 0.1, 4000)
    this.camera.position.set(0, 38, 62)

    this.renderer = new THREE.WebGLRenderer({ antialias: true, powerPreference: 'high-performance', stencil: false })
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    this.renderer.setSize(window.innerWidth, window.innerHeight)
    this.renderer.shadowMap.enabled = true
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping
    this.renderer.toneMappingExposure = 1.18
    this.renderer.outputColorSpace = THREE.SRGBColorSpace
    document.body.appendChild(this.renderer.domElement)
    this.renderer.domElement.style.position='fixed'
    this.renderer.domElement.style.inset='0'
    this.renderer.domElement.style.zIndex='1'

    // Lights — GTA VI sunset neon palette
    this.sun = new THREE.DirectionalLight(0xfff1c1, 2.8)
    this.sun.position.set(380, 620, -260)
    this.sun.castShadow = true
    this.sun.shadow.mapSize.set(CONFIG.render.shadowMap, CONFIG.render.shadowMap)
    this.sun.shadow.camera.near = 1
    this.sun.shadow.camera.far = 2400
    this.sun.shadow.camera.left = -900
    this.sun.shadow.camera.right = 900
    this.sun.shadow.camera.top = 900
    this.sun.shadow.camera.bottom = -900
    this.sun.shadow.bias = -0.0006
    this.scene.add(this.sun)

    this.hemi = new THREE.HemisphereLight(0xff6b9d, 0x0a1a3a, 0.85)
    this.hemi.position.set(0, 200, 0)
    this.scene.add(this.hemi)

    // Fill lights for neon bounce
    const fill1 = new THREE.PointLight(0xff2e8a, 800, 900)
    fill1.position.set(120, 45, 120)
    this.scene.add(fill1)
    const fill2 = new THREE.PointLight(0x00e5ff, 600, 800)
    fill2.position.set(-180, 38, -220)
    this.scene.add(fill2)

    this.moonLight = new THREE.DirectionalLight(0x8fb8ff, 0.55)
    this.moonLight.position.set(-420, 520, 320)
    this.scene.add(this.moonLight)

    this.clock = new THREE.Clock()
    this.time = 0
    this.delta = 0

    // Post-ish bloom via canvas filter trick + renderer bloom emulation with additive planes later
    this.composerEnabled = false

    window.addEventListener('resize', () => this.resize())
    this.resize()

    // Floor for shadow receive
    const groundMat = new THREE.MeshStandardMaterial({ color: 0x0f1b2e, roughness: 0.92 })
    this.groundReceive = groundMat
  }
  resize(){
    this.camera.aspect = window.innerWidth / window.innerHeight
    this.camera.updateProjectionMatrix()
    this.renderer.setSize(window.innerWidth, window.innerHeight)
  }
  render(){
    this.renderer.render(this.scene, this.camera)
  }
}
