#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
SDK="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}"
BT="$SDK/build-tools/35.0.0"
ANDROID="$SDK/platforms/android-35/android.jar"
mkdir -p build/classes build/dex
"$BT/aapt2" link -o build/base.apk -I "$ANDROID" --manifest app/src/main/AndroidManifest.xml -A app/src/main/assets -0 ogg
javac --release 8 -classpath "$ANDROID" -d build/classes app/src/main/java/com/offline/mancala/*.java
"$BT/d8" --lib "$ANDROID" --min-api 23 --output build/dex build/classes/com/offline/mancala/*.class
cp build/base.apk build/unsigned.apk
(cd build/dex && zip -q -u ../unsigned.apk classes.dex)
"$BT/zipalign" -f 4 build/unsigned.apk build/aligned.apk
if [ ! -f build/local.keystore ]; then
  keytool -genkeypair -keystore build/local.keystore -storepass android -keypass android -alias local -dname 'CN=Mancala Local' -keyalg RSA -validity 10000 >/dev/null 2>&1
fi
"$BT/apksigner" sign --ks build/local.keystore --ks-pass pass:android --out Mancala-Offline.apk build/aligned.apk
"$BT/apksigner" verify Mancala-Offline.apk
printf 'Built Mancala-Offline.apk\n'
