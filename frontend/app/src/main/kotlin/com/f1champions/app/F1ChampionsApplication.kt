package com.f1champions.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for F1 Champions app.
 * Annotated with [HiltAndroidApp] to enable Hilt dependency injection.
 */
@HiltAndroidApp
class F1ChampionsApplication : Application() 