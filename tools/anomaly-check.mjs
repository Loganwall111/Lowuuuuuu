import { build } from 'esbuild';
import fs from 'fs';

const out = await build({ entryPoints: ['src/bjs/systems/QuantumAnomalySystem.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error' });
const file = `/tmp/anomaly-${Date.now()}.mjs`;
fs.writeFileSync(file, out.outputFiles[0].text);
const { anomalyAt, ANOMALY_SECTOR_SIZE, ANOMALY_SENSOR_RANGE } = await import(file);
let pass = 0, fail = 0;
const ok = (name, condition) => {
  condition ? (pass++, console.log('  PASS  ' + name))
    : (fail++, console.log('  FAIL  ' + name));
};
const a = anomalyAt(0, 0, 0, 42);
const b = anomalyAt(0, 0, 0, 42);
const c = anomalyAt(ANOMALY_SECTOR_SIZE, 0, 0, 42);
console.log('\n— procedural spacetime cathedrals —');
ok('macro sectors span 260,000 units', ANOMALY_SECTOR_SIZE === 260000);
ok('sensor range is finite and positive', ANOMALY_SENSOR_RANGE > 0 && Number.isFinite(ANOMALY_SENSOR_RANGE));
ok('a sector is deterministic', a.id === b.id && a.center.equals(b.center));
ok('different sectors produce different events', a.id !== c.id);
ok('the home anomaly is composed near the opening vista', a.center.length() < 6000);
ok('anomaly radius is visible but bounded', a.radius > 400 && a.radius < 1800);
ok('frequency is physical and finite', a.frequency > 0 && Number.isFinite(a.frequency));
ok('classification is authored', ['CHRONO RIFT','VOID BLOOM','QUANTUM CHOIR'].includes(a.klass));
ok('invalid coordinates degrade safely', Number.isFinite(anomalyAt(NaN, Infinity, -Infinity).center.length()));
for (let i = -20; i <= 20; i++) {
  const q = anomalyAt(i * ANOMALY_SECTOR_SIZE, i * 13, -i * ANOMALY_SECTOR_SIZE, 9);
  ok('sector ' + i + ' remains finite', Number.isFinite(q.center.x + q.center.y + q.center.z + q.radius));
}
console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
