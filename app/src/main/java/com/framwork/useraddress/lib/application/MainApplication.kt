package com.framwork.useraddress.lib.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.framwork.useraddress.R
import com.framwork.useraddress.lib.base.BaseActivity
import com.framwork.useraddress.lib.dependencyinjection.APIComponent
import com.framwork.useraddress.lib.dependencyinjection.APIModule
import com.framwork.useraddress.lib.dependencyinjection.DaggerAPIComponent


class MainApplication : Application() {

    private var isAppOpen = false

    companion object {
        lateinit var instance: MainApplication
        lateinit var apiComponent: APIComponent
        lateinit var mCurrentActivity: BaseActivity
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        isAppOpen = true

        //Initialize Dagger
        apiComponent = DaggerAPIComponent.builder().aPIModule(APIModule(this)).build()

        //  Created notification channel to support notification on Android Oreo and Pie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                    getString(R.string.app_name),
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            )
            mChannel.description = getString(R.string.app_name)
            val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

    }

    override fun onTerminate() {
        super.onTerminate()
        isAppOpen = false
    }

    /**
     * @return The live application context
     */
    fun getContext(): Context {
        return applicationContext
    }

    /**
     * @return Instance of APIComponent
     */
    fun getAPIComponent(): APIComponent {
        return apiComponent
    }

    /**
     * @return Instance of current foreground activity
     */
    fun getCurrentForegroundActivity(): BaseActivity {
        return mCurrentActivity
    }
}