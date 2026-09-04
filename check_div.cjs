const fs = require('fs');
const c = fs.readFileSync('e:/实训学期/项目/-steampy/src/views/GameDetail.vue', 'utf8');
const start = c.indexOf('<template>') + 9;
const end = c.indexOf('</template>');
const tpl = c.slice(start, end);

let depth = 0;
const stack = [];
for (let i = 0; i < tpl.length; ) {
  const openMatch = tpl.slice(i).match(/^<div[\s>]/);
  const closeMatch = tpl.slice(i).match(/^<\/div>/);
  if (openMatch) {
    depth++;
    const line = tpl.slice(0, i).split('\n').length;
    stack.push({ depth, line, open: true, tag: openMatch[0] });
    i += openMatch[0].length;
  } else if (closeMatch) {
    if (stack.length === 0) {
      console.log(`ERROR: extra </div> at line ${tpl.slice(0, i).split('\n').length}`);
      break;
    }
    const last = stack.pop();
    depth--;
    const line = tpl.slice(0, i).split('\n').length;
    console.log(`L${line}: </div> closes L${last.line} (depth ${last.depth}→${depth})`);
    i += 6;
  } else if (tpl[i] === '<') {
    // skip self-closing or other tags
    const m = tpl.slice(i).match(/^<\/?[a-zA-Z][^>]*\/?>/);
    if (m) i += m[0].length; else i++;
  } else {
    i++;
  }
}
console.log(`\nFinal depth: ${depth}, remaining opens: ${stack.length}`);
if (stack.length) {
  stack.forEach(s => console.log(`  UNOPENED: L${s.line} depth ${s.depth}`));
}
