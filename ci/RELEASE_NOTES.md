# Devouring Storms: The Point of No Return — 1.9.120

Launch-crash fix, take three — final form. The jar declares TWO mixin
configs: the base mod's `dabywitherstormmod.mixins.json` and the original
author's `mcsm_extras.mixins.json`. 1.9.119's safety check refused to guess
which was which and failed the build; 1.9.120 identifies the base config by
its `package` key, extends only that one with the ten overlay mixins
(compiled into the same package, simple-name entries — Mixin's only valid
shape), and leaves the author's config byte-identical.

The CI gate verifies: every entry of every config resolves to a real class,
every config keeps its package key (Mixin orphans entries without one), and
each overlay mixin in the jar is the freshly compiled class.

Remove ALL older jars from `mods/`; install only
`devouringstorms-1.9.120-26.2-beta-ds.jar`. Expect
`[ds] Devouring Storms 1.9.120 loaded…` in chat.

Story Look resource pack attached below.
