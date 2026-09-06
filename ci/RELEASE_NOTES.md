# Devouring Storms: The Point of No Return — 1.9.118

**Second launch-crash fix — this one is definitive.** 1.9.117 still crashed
because Mixin prepends a config's declared `package` to *every* entry, even
fully-qualified ones. 1.9.118 rewrites the mixin config at build time the
only safe way: the `package` key is dropped and every entry (base mod's and
ours) becomes fully qualified, so each one resolves to exactly one real
class.

The CI gate now mirrors Mixin's real resolution rule and fails the build if
any entry would not resolve — 1.9.114–1.9.117 could not pass it.

Remove ALL older jars from `mods/`; install only
`devouringstorms-1.9.118-26.2-beta-ds.jar`. Expect
`[ds] Devouring Storms 1.9.118 loaded…` in chat.

Story Look resource pack (stacked cloud decks, pastel lighting, halo sun)
attached below as `devouringstorms-storylook-1.9.118.zip`.
