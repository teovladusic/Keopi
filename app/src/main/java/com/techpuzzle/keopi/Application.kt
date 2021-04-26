package com.techpuzzle.keopi

import android.app.Application
import com.techpuzzle.keopi.BuildConfig.MONGODB_REALM_APP_ID
import dagger.hilt.android.HiltAndroidApp
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration

lateinit var keopiApp: App

@HiltAndroidApp
class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        keopiApp = App(AppConfiguration.Builder(MONGODB_REALM_APP_ID).build())
    }
}