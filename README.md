# Mancala Offline for Android

Open `Mancala-Offline.apk` on an Android 6.0 or newer device to install. If Android asks, allow installation from the app used to open the APK.

- Fully offline native Android app; no internet permission, ads, or accounts.
- Wooden tabletop home screen with a large board preview, Play solo, Two players, Continue game, rules, and Settings.
- Three selectable board finishes: Honey oak, American walnut, and Rosewood, each with a coordinated stone palette. Your choice is saved across launches.
- Immersive landscape play: the board fills the area below the toolbar, with tall recessed pits, readable counts, and player scores between the rows. New game is available in Settings.
- Oversized pebbles, hearts, or stars: each new game chooses a different random piece style, preserved when resuming a saved game. Pieces retain their proportions as the board adapts to the screen.
- Custom warm-toned dialogs for difficulty, rules, confirmations, settings, and rating availability.
- Bundled CC0 background music (Calm Track by pmiller) and Kenney effects for buttons, pickup, stone drops, captures, wins, losses, and draws. Music and effects have independent saved toggles. Audio pauses in the background and respects Android audio focus.
- Audio credits are available in Settings and AUDIO_CREDITS.md; source notices and hashes ship inside the APK. In two-player mode, either player’s victory uses the win jingle.
- Rate us explains that ratings are unavailable until a Google Play listing exists.
- Entire app stays in landscape, including the home screen and dialogs.
- Dedicated landscape game screen with a selectable textured wooden board, recessed pits, colored stones, and glowing legal moves.
- Animated hand picks up and sows each move, including bot moves, with a remaining-stone counter.
- Board and hand are drawn natively with Canvas; no downloaded images or runtime dependencies.
- Easy bot picks random legal moves.
- Expert bot uses time-bounded iterative-deepening minimax with alpha-beta pruning (up to 16 plies, about 700 ms per move). It is not guaranteed unbeatable.
- Two players share one device: Player 1 uses the bottom row/right store; Player 2 uses the upper row/left store.
- Standard Kalah, four stones per pit, captures, extra turns, and final sweep.
- Automatically saves the current game and mode, including across rotation.

## Build

Requires a JDK, Android SDK platform 35, and build-tools 35.0.0. Run `./build.sh`; set `ANDROID_SDK_ROOT` if the SDK is not at `~/Library/Android/sdk`.

This project uses the Android SDK directly without external dependencies or Gradle. Java source is in `app/src/main/java/com/offline/mancala`. The local signing key is generated in `build/local.keystore`; retain it to install future builds as updates. This is a locally signed APK, not a Play Store release.

## Engine tests

```sh
mkdir -p build/test-classes
javac --release 8 -d build/test-classes app/src/main/java/com/offline/mancala/Game.java tests/GameTest.java
java -cp build/test-classes com.offline.mancala.GameTest
```

Tests cover extra turns, captures, skipping the opponent's store, final sweeping, stone conservation and termination across 200 full games, plus Expert bot legality and search timing.

Validation completed: APK signature verified; engine tests passed; Android API 36 emulator install and launch passed. UI smoke tests passed for extra turns, both bot difficulties, two-player turn handoff, and saved-game restoration after force-stop/restart. Physical-device testing has not been performed.

Audio validation: `bash tests/run_audio_test.sh` installs a separate instrumentation test APK on a connected emulator. All eight bundled files decoded successfully; background playback, mute, and pause passed; all seven effects loaded and played; win/loss/draw and local-player result mappings passed.

## November UI refresh

The home and game screens use a generated walnut tabletop with three board finishes. Preview captures are in `build/home-redesign.png`, `build/home-compact.png`, and `build/phone-board-v2.png`. The compact home layout keeps all play actions visible on a 640 × 360 dp landscape screen.

Validation: rebuilt and signature-verified APK; engine tests passed; emulator checks passed for extra turns, local-player handoff, saved-game resume, Expert animated response, all three board choices, and board preference persistence after force-stop. Home layouts were visually inspected at normal and compact sizes. Physical-device testing remains outstanding.

Phone UX refinement: removed the game footer, reduced game margins, applied visible system-bar/cutout insets, corrected difficulty-card baseline alignment, reduced dialog typography, and enlarged pieces. The board now adapts to both available width and height; hit testing uses the same transforms.

The full-board phone layout was verified on the Small Phone emulator at its native 720 × 1280 resolution (320 dpi, landscape). UI smoke checks passed for the new coordinate mapping, extra turns, two-player handoff, saved-game resume, and Expert animated replies.
