package com.callmydd.mvvm

import android.app.Activity
import android.app.Application
import com.callmydd.mvvm.di.DaggerAppComponent
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject


class MVVMApplication : Application(), HasActivityInjector {

    @set:Inject var androidInjector: DispatchingAndroidInjector<Activity>? = null

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder().application(this)
            .build().inject(this)
    }

    override fun activityInjector(): DispatchingAndroidInjector<Activity>? {
        return androidInjector
    }
}