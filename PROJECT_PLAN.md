# EchoOn – Project Plan

## Summary
- **App:** EchoOn – unified text, voice, and camera translator (Android only).
- **Monetization:** Free with ads (AdMob). No RevenueCat for now.
- **Backend:** Supabase (auth + optional history). Keys stored in `.env`.

---

## Phase 1: Setup & basic app structure ✅ (current)
- Create Android project (Kotlin + Jetpack Compose).
- MVVM structure, light/dark theme, navigation skeleton.
- **Trinity UI** home screen: three big actions – **See** (camera), **Hear** (audio), **Write** (text).
- Placeholder screens for each mode; no real translation yet.
- **How to test:** Open project in Android Studio → Run on emulator or device.

---

## Phase 2: Language picker & text translation
- Language picker (source + target).
- Text translation (API or on-device; TBD).
- “Speak translation aloud” (TTS).
- **How to test:** Type text → choose languages → translate → play TTS.

---

## Phase 3: Voice translation
- Speech-to-text (STT) → translate → Text-to-speech (TTS) “Echo”.
- Haptic feedback: Listening / Thinking / Ready.
- **How to test:** Tap Hear → speak → hear translation.

---

## Phase 4: Camera translation (instant, no photo sent)
- Camera permission and live preview.
- On-device OCR (e.g. ML Kit) and overlay/panel translation.
- No image sent to server (privacy-first).
- **How to test:** Point at text → see translation on screen.

---

## Phase 5: Supabase & optional auth
- Connect Supabase (URL + anon key from `.env`).
- Optional login/signup (e.g. for history/sync later).
- Optional: save translation history in Supabase with RLS.
- **How to test:** Sign up, log in, (optional) see history.

---

## Phase 6: Ads & polish
- Integrate AdMob (banner and/or interstitial).
- Auto language detection where feasible.
- Haptic and UX polish, dark mode check.
- **How to test:** See ads in allowed placements; full flow on device.

---

*After each phase, confirm it works on your side before moving to the next.*
