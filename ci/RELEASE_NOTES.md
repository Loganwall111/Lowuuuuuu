# Devouring Storms: The Point of No Return — 1.9.119

**Third and final launch-crash fix — this one matches Mixin's actual rules,
verified against your two crash logs.** Mixin configs have exactly one valid
shape: a `package` key plus simple-name entries. 1.9.117 broke rule one
(FQ entries got the package prepended again); 1.9.118 broke rule two (no
package key ⇒ Mixin orphans EVERY entry — the base mod's own mixins stopped
loading, hence the `ItemTintSourcesAccessor` AssertionError).

1.9.119 keeps the base config's `package` untouched and compiles the ten
overlay mixins INTO that same package, so every entry resolves to exactly
one real, freshly-compiled class. The CI gate now also proves each overlay
mixin class in the jar is the fresh overlay (not a stale base copy), and
fails on any package-less config.

Remove ALL older jars from `mods/`; install only
`devouringstorms-1.9.119-26.2-beta-ds.jar`. Expect
`[ds] Devouring Storms 1.9.119 loaded…` in chat.

Story Look resource pack attached below.
