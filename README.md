<p align="center"><img src="docs/images/icon.png" alt="RateGuru" width="180"></p>

# RateGuru

A currency converter for travelers, with no ads. It works offline, needs no account, and doesn't ask for location access.

## What it does

- **Many currencies at once.** Up to 8 on one screen, all converting together. Tap any row to type in it, and tap a flag to change or remove that currency.
- **Opens on the currency you need.** When you land somewhere new, RateGuru picks the local currency from the mobile network and puts it at the top.
- **Calculator keypad.** Do sums like `45000 × 3`, add a tip with `+ 10%`, and use the `000` key for currencies with big notes.
- **Rule of thumb.** For example, "Drop 3 zeros, then ÷ 26" for Vietnamese dong to US dollars.
- **Works offline.** It keeps the last rates it downloaded, and ships with a snapshot of 166 currencies.
- **Light and dark mode**, and it updates itself from GitHub releases.

## Download

Get the latest APK from [Releases](https://github.com/getguruhat/RateGuru/releases/latest/download/RateGuru.apk).

## Rates

Mid-market rates, updated once a day, come from [Exchange Rate API](https://www.exchangerate-api.com) (open access). If it's down, the app uses a jsDelivr mirror of [fawazahmed0/exchange-api](https://github.com/fawazahmed0/exchange-api). Banks, cards and exchange booths add their own margin on top.

## Building

Builds run on GitHub Actions only. Every push builds an APK. Pushing a `vX.Y.Z` tag publishes a signed release, and installed copies offer to update.

MIT licensed. Made by [GuruHat](https://guruhat.com).
