/* ============================================================
   GRAVITON 3D :: puzzles — math gates & alien-language ciphers
   ============================================================ */
const Puzzles3D = (() => {
  const GLYPH=['◈','◉','▣','◆','◍','⊕','✶','❖','◑','⊛','✧','◆','▤','◐','❋','✢','◫','⬢','◯','◬','✦','❁','◪','⊚','◭','◸'];

  function caesarShift(str,n){
    return str.split('').map(c=>{
      const code=c.charCodeAt(0);
      if(code>=65&&code<=90) return String.fromCharCode(65+(code-65+n+26)%26);
      if(code>=97&&code<=122) return String.fromCharCode(97+(code-97+n+26)%26);
      return c;
    }).join('');
  }

  function randomMath(diff){
    const ops=['+','−','×','÷'];
    let a,b,op,answer,display;
    if(diff===0){ op=U3.pick(['+','−']); a=U3.randInt(2,20); b=U3.randInt(2,20); answer=op==='+'?a+b:a-b; display=`${a} ${op} ${b}`; }
    else if(diff===1){
      op=U3.pick(['+','−','×']); a=U3.randInt(3,12); b=U3.randInt(3,12);
      answer=op==='×'?a*b : op==='−'?a-b : a+b; display=`${a} ${op} ${b}`;
    } else {
      const k=U3.randInt(0,3);
      if(k===0){ a=U3.randInt(6,16); answer=a*a; display=`${a}²`; }
      else if(k===1){ a=U3.randInt(3,13); answer=a; display=`√${a*a}`; }
      else if(k===2){ a=U3.randInt(2,10)*10; answer=a/4; display=`${a} ÷ 4`; }
      else { a=U3.randInt(7,15); b=U3.randInt(7,15); answer=a*b; display=`${a} × ${b}`; }
    }
    while(answer<0){ a=Math.abs(a)+2; answer=a-b; }
    return { type:'math', display, answer };
  }

  function randomCipher(diff){
    const words = diff===0
      ? ['STAR','MOON','VOID','LIGHT','ORBIT','MASS','AXIS','GATE','HOPE','PULSE','SPIN','FLUX']
      : ['GRAVITY','SINGULAR','EVENT','PHOTON','NEBULA','COSMOS','QUASAR','PRISM','TIDAL','LENS'];
    const word=U3.pick(words);
    if(diff===0){
      const shift=U3.randInt(1,5);
      const cipher=caesarShift(word,shift);
      const glyph=cipher.split('').map(c=>GLYPH[c.charCodeAt(0)-65]).join('');
      return { type:'cipher', word, glyph, hint:`Letters shifted by ${shift}`, table:[] };
    } else {
      const glyph=word.split('').map(ch=>GLYPH[ch.charCodeAt(0)-65]).join('');
      const table=U3.shuffle(word.split('')).slice(0,3).map(ch=>({k:ch, v:GLYPH[ch.charCodeAt(0)-65]}));
      return { type:'cipher', word, glyph, hint:'Use the cipher table', table };
    }
  }

  function random(sector){
    const diff=Math.min(2, Math.floor(sector/2));
    const kind=Math.random()<0.5?'math':'cipher';
    return kind==='math'? randomMath(diff) : randomCipher(diff);
  }

  return { random };
})();
