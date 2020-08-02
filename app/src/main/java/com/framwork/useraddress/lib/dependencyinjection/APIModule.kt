package com.framwork.useraddress.lib.dependencyinjection

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.framwork.useraddress.lib.application.MainApplication
import com.framwork.useraddress.lib.preferrences.AppData
import com.framwork.useraddress.lib.retrofit.APIService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author Aditi Shirsat
 */
@Module
class APIModule(
    @get:Provides
    @get:Singleton
    internal val app: MainApplication
) {
    internal val meGson: Gson
        @Provides
        @Singleton
        get() = Gson()

    internal val apiServices: APIService.Companion
        @Provides
        @Singleton
        get() = APIService.Companion

    internal val ApiService: APIService
        @Provides
        @Singleton
        get() = APIService.getBaseUrl()

    internal val appData: AppData
        @Provides
        @Singleton
        get() = AppData(app)

    @Provides
    @Singleton
    internal fun providePreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app)
    }
}
