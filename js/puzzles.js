/* ============================================================
   GRAVITON :: puzzles — math gates & alien-language ciphers
   ============================================================ */
const Puzzles = (() => {

  /* ---------------- MATH ---------------- */
  function randomMath(difficulty){ // difficulty 0..2
    const ops=['+','−','×','÷','^'];
    let a,b,op,answer,display;
    if(difficulty===0){
      op=ops[Util.randInt(0,1)];
      a=Util.randInt(2,20); b=Util.randInt(2,20);
      answer = op==='+'?a+b : a-b;
      display=`${a} ${op} ${b}`;
    } else if(difficulty===1){
      op=ops[Util.randInt(0,2)];
      if(op==='×'){ a=Util.randInt(3,12); b=Util.randInt(3,12); answer=a*b; }
      else if(op==='−'){ a=Util.randInt(10,40); b=Util.randInt(1,10); answer=a-b; }
      else { a=Util.randInt(2,9); b=Util.randInt(0, a); answer=a+b; }
      display=`${a} ${op} ${b}`;
    } else {
      const kinds=Util.randInt(0,4);
      if(kinds===0){ // square
        a=Util.randInt(6,16); answer=a*a; display=`${a}²`;
      } else if(kinds===1){ // sqrt
        a=Util.randInt(3,13); answer=a; display=`√${a*a}`;
      } else if(kinds===2){ // fraction-ish percent
        a=Util.randInt(2,10)*10; answer=a/4; display=`${a} ÷ 4`;
      } else if(kinds===3){ // multiply
        a=Util.randInt(7,15); b=Util.randInt(7,15); answer=a*b; display=`${a} × ${b}`;
      } else { // square
        a=Util.randInt(11,20); answer=a*a; display=`${a}²`;
      }
    }
    // negative answers discouraged
    while(answer<0){ a=Math.abs(a)+2; answer=a-b; }
    return {type:'math', display, answer, prompt:'Unlock the jump gate · solve the equation'};
  }

  /* ---------------- LANGUAGE CIPHER ---------------- */
  // Generate a deterministic pseudo-alien glyph alphabet using an offset + symbol set.
  const GLYPH_BASE=['◈','◉','▣','◆','◍','⊕','✶','❖','◑','⊛','✧','◆','▤','◐','❋','✢','◫','⬢','◯','◬','✦','❁','◪','⊚','◭','◸'];
  const GLYPH = GLYPH_BASE;

  function caesarShift(str, n){
    return str.split('').map(c=>{
      const code=c.charCodeAt(0);
      if(code>=65&&code<=90){ return String.fromCharCode(65+(code-65+n+26)%26); }
      if(code>=97&&code<=122){ return String.fromCharCode(97+(code-97+n+26)%26); }
      return c;
    }).join('');
  }

  // Build a cipher where each plaintext letter maps to a glyph (or shifted letter).
  function randomCipher(difficulty){
    const plainWords = difficulty===0
      ? ['STAR','MOON','VOID','LIGHT','ORBIT','MASS','AXIS','GATE','HOPE','PULSE','SPIN','FLUX']
      : ['GRAVITY','SINGULAR','EVENT','PHOTON','NEBULA','COSMOS','QUASAR','PRISM','TIDAL','LENS'];
    const word = plainWords[Util.randInt(0,plainWords.length-1)];

    if(difficulty===0){
      // simple Caesar offset; show glyph for each letter, ask decode
      const shift=Util.randInt(1,5);
      const cipher=caesarShift(word, shift);
      // build glyph string
      const glyphStr=cipher.split('').map(c=>{
        const idx=c.charCodeAt(0)-65;
        return GLYPH[idx];
      }).join('');
      return {
        type:'cipher', word, cipher, shift, glyph:glyphStr,
        prompt:'Decode the Sigil of the Ancients (Caesar cipher)',
        display:glyphStr,
        hint:`Letters shifted by ${shift}`,
      };
    } else {
      // substitution: each plaintext letter -> a glyph
      const used=new Set();
      const mapping={};
      for(const ch of word){
        const idx=ch.charCodeAt(0)-65;
        let g=GLYPH[idx];
        mapping[ch]=g;
        used.add(g);
      }
      const glyphStr=word.split('').map(ch=>mapping[ch]).join('');
      // provide partial table of known mappings
      const table=[];
      const sampleLetters=Util.shuffle(word.split('')).slice(0, Math.min(3, word.length));
      for(const ch of sampleLetters){
        table.push({k:ch, v:mapping[ch]});
      }
      return {
        type:'cipher', word, mapping, glyph:glyphStr, table,
        prompt:'Decode the Sigil of the Ancients',
        display:glyphStr,
        hint:'Use the cipher table',
      };
    }
  }

  // Determine difficulty by sector
  function difficultyFor(sector){ return Math.min(2, Math.floor((sector)/2)); }

  function random(sector, kind){
    const diff=difficultyFor(sector);
    return kind==='math' ? randomMath(diff) : randomCipher(diff);
  }

  return { random, difficultyFor };
})();
