import fs from 'fs';
import path from 'path';

const root = path.resolve('dist');
const base = (process.argv[2] || '/Low/').replace(/\/+$/, '/') ;
const textExtensions = new Set(['.html', '.js', '.css', '.map']);

function visit(dir) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const file = path.join(dir, entry.name);
    if (entry.isDirectory()) visit(file);
    else if (textExtensions.has(path.extname(entry.name))) {
      const before = fs.readFileSync(file, 'utf8');
      const after = before
        // Runtime-created Texture/CSS URLs are not rewritten by Vite.
        .replaceAll('/art/', base + 'art/')
        // index.html links are rewritten by Vite already; only fix a truly
        // root-relative favicon and never turn /Low/ into /Low/Low/.
        .replace(/(["'(])\/favicon\.svg/g, `$1${base}favicon.svg`);
      if (after !== before) fs.writeFileSync(file, after);
    }
  }
}

visit(root);
// GitHub Pages serves this fallback for direct links and refreshes.
fs.copyFileSync(path.join(root, 'index.html'), path.join(root, '404.html'));
console.log(`Prepared ${root} for GitHub Pages at ${base}`);
