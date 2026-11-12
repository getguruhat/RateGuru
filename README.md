<p align="center"><img src="docs/images/icon.png" alt="RateGuru" width="180"></p>

# RateGuru

A currency converter for travelers, with no ads. It works offline, needs no account, and doesn't ask for location access.

## What it does

- **Many currencies at once.** Up to 8 on one screen, all converting together. Tap any row to type in it, and tap a flag to change or remove that currency.
- **Opens on the currency you need.** When you land somewhere new, RateGuru picks the local currency from the mobile network and puts it at the top.
- **Simple keypad.** Four rows, like a classic converter. Tap the calculator key for + − × ÷ % (sums like `45000 × 3`, a tip with `+ 10%`). Hold 0 for 000, and hold ⌫ to clear.
- **Works offline.** It keeps the last rates it downloaded, and ships with a snapshot of 166 currencies.
- **Dark charcoal look with raised 3D keys**. The GitHub APK updates itself from GitHub releases.

## Download

Get the latest APK from [Releases](https://github.com/getguruhat/RateGuru/releases/latest/download/RateGuru.apk), or install it from F-Droid once it is listed there.

## Support

RateGuru is free and has no ads. If it saved you some mental math, [star the repo on GitHub](https://github.com/getguruhat/RateGuru) so other travelers can find it. Questions and ideas are welcome in [Discussions](https://github.com/getguruhat/RateGuru/discussions), and bugs go in [Issues](https://github.com/getguruhat/RateGuru/issues).

## Rates

Mid-market rates, updated once a day, come from [Exchange Rate API](https://www.exchangerate-api.com) (open access). If it's down, the app uses a jsDelivr mirror of [fawazahmed0/exchange-api](https://github.com/fawazahmed0/exchange-api). Banks, cards and exchange booths add their own margin on top.

## Building

Builds run on GitHub Actions only. Every push builds an APK. Pushing a `vX.Y.Z` tag publishes a signed release, and installed copies of the GitHub APK offer to update.

There are two flavors. `github` is the signed release APK with the self-updater. `fdroid` has no updater and no install permission, and is what F-Droid builds (`./gradlew assembleFdroidRelease`).

MIT licensed. Made by [GuruHat](https://guruhat.com).

Guide: [How to convert currency while traveling, offline and without ads](https://guruhat.com/guides/currency-converter-offline-travel-android/). More at [guruhat.com/apps/rateguru](https://guruhat.com/apps/rateguru/).
