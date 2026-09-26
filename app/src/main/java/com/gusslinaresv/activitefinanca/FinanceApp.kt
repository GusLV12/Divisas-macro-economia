package com.gusslinaresv.activitefinanca

import android.app.Application
import com.gusslinaresv.activitefinanca.data.UserPreferences

class FinanceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        UserPreferences.init(this)
    }
}
