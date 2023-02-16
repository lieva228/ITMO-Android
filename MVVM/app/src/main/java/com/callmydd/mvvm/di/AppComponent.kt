package com.callmydd.mvvm.di

import android.app.Application
import com.callmydd.mvvm.MVVMApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton


@Singleton
@Component(modules = [AndroidInjectionModule::class,
    AppModule::class,
    MainActivityModule::class])
interface AppComponent : AndroidInjector<MVVMApplication> {
    @Component.Builder
    interface Builder {
        @BindsInstance fun application(application: Application): Builder
        fun build(): AppComponent
    }
    override fun inject(app: MVVMApplication)
}