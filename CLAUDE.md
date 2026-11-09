@AGENTS.md

# Claude notes

Use `AGENTS.md` as the source of truth. Keep changes scoped: this is an Arabic-only Compose game
that plays offline, with AdMob limited to a between-round interstitial and an opt-in rewarded hint.
Run `./gradlew :app:testDebugUnitTest` after touching game logic or content, and
`./gradlew :app:assembleDebug` before reporting completion.
