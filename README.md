# 🥬 Sprout Atlas 

**Sprout Atlas** is an AI-powered produce field guide and visual freshness scanner built to end grocery waste. 

Ever stood in the grocery aisle wondering if an avocado is actually ripe, or how to wash pesticides off your grapes? I built Sprout Atlas to solve that exact problem. Just snap a photo, and in under three seconds, you get actionable data on freshness, storage, and safety.

Built by a solo developer for the **Shipaton 2026 NextGen Category** and **Build in Public Award**.

## ✨ What It Does

*   📸 **Visual Freshness Scanner:** Point your camera at any produce to instantly know if it's at its peak.
*   🛡️ **Safe Washing & Chemical Info:** Clear instructions on how to properly wash specific fruits and vegetables.
*   📖 **Produce Encyclopedia & Field Guide:** Dive deep into seasonal availability guides and nutritional facts.
*   🧠 **Daily Quiz:** Test your knowledge on seasonal eating and produce care.

## 🛠️ The Tech Stack

*   **Frontend:** Flutter (Cross-platform iOS & Android)
*   **AI Engine:** Google Gemini AI (Powers the visual scanning and instant analysis)
*   **Monetization:** RevenueCat (Manages Sprout Atlas Pro subscriptions and paywalls)

## 🚀 The Build in Public Journey

Building this solo for Shipaton has been a massive learning curve. 
*   **The biggest win:** Getting Gemini to accurately identify tricky produce variations in under three seconds.
*   **The biggest challenge:** Navigating Android package names and RevenueCat service accounts to ensure a seamless Pro upgrade experience. 

## 💻 How to Run Locally

1. Clone this repository.
2. Run `flutter pub get` to install dependencies.
3. Add your RevenueCat Public SDK Key (`goog_...`) to the initialization file.
4. Run `flutter run`.
