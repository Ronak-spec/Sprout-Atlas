package com.example

import android.app.Application
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.LogLevel

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            val rcKey = try {
                BuildConfig.REVENUECAT_PUBLIC_KEY
            } catch (e: Throwable) {
                ""
            }
            if (rcKey.isNotBlank() && !rcKey.startsWith("goog_YOUR_")) {
                Purchases.logLevel = LogLevel.WARN
                Purchases.configure(PurchasesConfiguration.Builder(this, rcKey).build())
            }
        } catch (e: Exception) {
            android.util.Log.w("MainApplication", "Purchases configuration skipped or failed: ${e.message}")
        }
    }
}
