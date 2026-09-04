package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
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
            val hasPlayBilling = isPlayBillingSupported(this)
            if (hasPlayBilling && rcKey.isNotBlank() && !rcKey.startsWith("goog_YOUR_")) {
                Purchases.logLevel = LogLevel.WARN
                Purchases.configure(PurchasesConfiguration.Builder(this, rcKey).build())
            } else {
                android.util.Log.i(
                    "MainApplication",
                    "Google Play Billing is not available on this environment/device. Local sandbox billing mode active."
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("MainApplication", "Purchases configuration skipped or failed: ${e.message}")
        }
    }

    private fun isPlayBillingSupported(context: Context): Boolean {
        return try {
            val intent = Intent("com.android.vending.billing.InAppBillingService.BIND").apply {
                setPackage("com.android.vending")
            }
            val services = context.packageManager.queryIntentServices(intent, 0)
            !services.isNullOrEmpty()
        } catch (t: Throwable) {
            false
        }
    }
}
