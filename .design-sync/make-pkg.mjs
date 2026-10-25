// Generates frontend/node_modules/homie-ui: a pseudo-installed package the
// design-sync converter reads like a published DS (Homie is an app, it has no
// library build). Run from frontend/ after `npm run build` (see cfg.buildCmd).
//   index.ts          re-exports the synced components (esbuild bundles it)
//   types/            .d.ts emitted by tsc from src/ + an index.d.ts barrel
//   styles.css        the compiled vite stylesheet (Tailwind utilities + tokens)
import { execSync } from 'node:child_process';
import { copyFileSync, mkdirSync, readdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { dirname, join, relative, sep } from 'node:path';

// shadcn primitives: every named export of these files.
const UI = ['button', 'card', 'dialog', 'input', 'label', 'select', 'sheet', 'switch', 'textarea', 'toggle', 'toggle-group'];
// Recipe components (default exports). Dialogs needing API/auth stay out.
const RECIPE = ['Recipe', 'RecipeCard', 'RecipeListItem', 'RecipeCollection', 'RecipeDetails', 'PortionStepper', 'RecipeViewToggle', 'IngredientList', 'EditableList'];

const PKG = join('node_modules', 'homie-ui');
rmSync(PKG, { recursive: true, force: true });
mkdirSync(join(PKG, 'types'), { recursive: true });

const barrel = (base) => [
  ...UI.map((f) => `export * from '${base}/Components/ui/${f}';`),
  ...RECIPE.map((n) => `export { default as ${n} } from '${base}/Components/Recipe/${n}';`),
].join('\n') + '\n';

writeFileSync(join(PKG, 'index.ts'), barrel('../../src'));
writeFileSync(join(PKG, 'types', 'index.d.ts'), barrel('./src'));
writeFileSync(join(PKG, 'package.json'), JSON.stringify({
  name: 'homie-ui', version: '0.0.0', private: true,
  module: 'index.ts', types: 'types/index.d.ts',
}, null, 2) + '\n');

execSync(`npx tsc -p tsconfig.app.json --noEmit false --declaration --emitDeclarationOnly --rootDir src --outDir ${PKG}/types/src`, { stdio: 'inherit' });

// tsc keeps `@/` path aliases in the .d.ts output; the converter's type
// resolver doesn't read tsconfig paths, so make them relative (otherwise e.g.
// ToggleGroup loses its `variant`/`size` props from toggleVariants).
const typesSrc = join(PKG, 'types', 'src');
const walk = (dir) => readdirSync(dir, { withFileTypes: true }).flatMap((e) =>
  e.isDirectory() ? walk(join(dir, e.name)) : e.name.endsWith('.d.ts') ? [join(dir, e.name)] : []);
for (const file of walk(typesSrc)) {
  const text = readFileSync(file, 'utf8');
  const fixed = text.replace(/(["'])@\/([^"']+)\1/g, (_, q, target) => {
    let rel = relative(dirname(file), join(typesSrc, target)).split(sep).join('/');
    if (!rel.startsWith('.')) rel = `./${rel}`;
    return `${q}${rel}${q}`;
  });
  if (fixed !== text) writeFileSync(file, fixed);
}

const css = readdirSync(join('dist', 'assets')).filter((f) => f.endsWith('.css'));
if (css.length !== 1) throw new Error(`expected one css in dist/assets, found ${css.length} - run npm run build`);
copyFileSync(join('dist', 'assets', css[0]), join(PKG, 'styles.css'));
console.log(`homie-ui written to ${PKG}`);
