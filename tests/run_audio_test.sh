#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/.."
SDK="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}"
BT="$SDK/build-tools/35.0.0"
ANDROID="$SDK/platforms/android-35/android.jar"
mkdir -p build/audio-test/classes build/audio-test/dex
javac --release 8 -cp "$ANDROID:build/classes" -d build/audio-test/classes tests/AudioInstrumentation.java
"$BT/aapt2" link -o build/audio-test/test.apk -I "$ANDROID" --manifest tests/AudioTestManifest.xml
"$BT/d8" --lib "$ANDROID" --classpath build/classes --min-api 23 --output build/audio-test/dex build/audio-test/classes/com/offline/mancala/*.class
(cd build/audio-test/dex && zip -q -u ../test.apk classes.dex)
"$BT/zipalign" -f 4 build/audio-test/test.apk build/audio-test/aligned.apk
"$BT/apksigner" sign --ks build/local.keystore --ks-pass pass:android --out build/audio-test/signed.apk build/audio-test/aligned.apk
"$SDK/platform-tools/adb" install -r build/audio-test/signed.apk
"$SDK/platform-tools/adb" shell am instrument -w com.offline.mancala.audiotest/com.offline.mancala.AudioInstrumentation
