export const CONFIG = {
  world: {
    size: 1800,
    blockSize: 85,
    roadWidth: 18,
    sidewalk: 4,
    buildingMargin: 6,
    districts: [
      { name: 'Ocean Beach', center: [0, -520], radius: 520, density: 0.55, height: [14, 38], neon: 0.7, palette: 'pastel' },
      { name: 'Vice Downtown', center: [0, 80], radius: 560, density: 0.95, height: [60, 220], neon: 1.0, palette: 'neon' },
      { name: 'Wynwood Arts', center: [-520, 120], radius: 380, density: 0.75, height: [18, 72], neon: 0.5, palette: 'industrial' },
      { name: 'Brickell Keys', center: [520, 220], radius: 360, density: 0.8, height: [40, 160], neon: 0.6, palette: 'glass' },
    ]
  },
  player: { speed: 5.2, sprint: 8.5, jump: 7.2, gravity: 24 },
  vehicle: { maxSpeed: 42, accel: 18, brake: 28, steer: 1.55, grip: 0.92 },
  render: { fogNear: 380, fogFar: 1650, shadowMap: 2048 },
  gameplay: { wantedDecay: 0.18, pedCount: 54, trafficCount: 22, maxWanted: 6 }
}
export const RADIO_STATIONS = [
  { name: 'V-Rock • Vice City Vibes', color: '#ff2e8a', freq: '103.2 FM' },
  { name: 'Radio Leonida — Espantoso', color: '#00e5ff', freq: '98.6 FM' },
  { name: 'Vice Wave • Night Drive', color: '#7c4dff', freq: '89.4 FM' },
  { name: 'Head Radio • Pop', color: '#ffd600', freq: '102.3 FM' },
]
