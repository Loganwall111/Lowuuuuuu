/* ============================================================
   GRAVITON 3D :: audio — reuses the procedural WebAudio engine
   from the 2D build (js/audio.js). Provides ability SFX.
   ============================================================ */
const Audio3D = (() => {
  // init/resume delegates to the shared engine
  function init(){ AudioEngine.init(); AudioEngine.resume(); }

  const sfx = {
    shard(){ AudioEngine.sfx.shard(); },
    boost(){ AudioEngine.sfx.boost(); },
    puzzle(){ AudioEngine.sfx.puzzle(); },
    good(){ AudioEngine.sfx.good(); },
    bad(){ AudioEngine.sfx.bad(); },
    warp(){ AudioEngine.sfx.warp(); },
    dilate(){ AudioEngine.sfx.dilate(); },
    warning(){ AudioEngine.sfx.warning(); },
    ability(){ AudioEngine.sfx.good(); },
    crash(){ AudioEngine.sfx.bad(); },
    gate(){ AudioEngine.sfx.puzzle(); },
  };

  return { init, sfx };
})();
