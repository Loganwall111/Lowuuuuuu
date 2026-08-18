import{S as e}from"./index-jmZ0cpad.js";const a="mainUVVaryingDeclaration",r=`#ifdef MAINUV{X}
varying vMainUV{X}: vec2f;
#endif
`;e.IncludesShadersStoreWGSL[a]||(e.IncludesShadersStoreWGSL[a]=r);const i={name:a,shader:r},n="logDepthDeclaration",t=`#ifdef LOGARITHMICDEPTH
uniform logarithmicDepthConstant: f32;varying vFragmentDepth: f32;
#endif
`;e.IncludesShadersStoreWGSL[n]||(e.IncludesShadersStoreWGSL[n]=t);const s={name:n,shader:t};export{s as l,i as m};
