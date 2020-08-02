package com.framwork.useraddress.lib.dependencyinjection

import com.google.gson.Gson
import com.framwork.useraddress.lib.application.MainApplication
import com.framwork.useraddress.lib.preferrences.AppData
import com.framwork.useraddress.lib.retrofit.APIService
import dagger.Component
import javax.inject.Singleton

/**
 * @author Aditi Shirsat
 */

@Singleton
@Component(modules = [APIModule::class])
interface APIComponent {
    val gson: Gson
    val appData: AppData
    val retroService: APIService.Companion

    val app: MainApplication
}
