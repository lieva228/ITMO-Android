package com.callmydd.mvvm.di

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.callmydd.mvvm.api.ApiService
import com.callmydd.mvvm.model.AppDatabase
import com.callmydd.mvvm.model.MessageDao
import com.callmydd.mvvm.utils.BASE_URL
import com.callmydd.mvvm.utils.ROOM_DB_NAME
import com.callmydd.mvvm.viewModel.ViewModelsFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module(subcomponents = [ViewModelSubComponent::class])
internal class AppModule() {

    @Singleton
    @Provides
    fun provideRetrofit(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(MoshiConverterFactory.create())
            .build().create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMessageDao(app: Application): MessageDao =
        Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            ROOM_DB_NAME
        ).build().messageDao!!.also { println("data done") }

    @Singleton
    @Provides
    fun provideViewModelsFactory (
        viewModelSubComponent: ViewModelSubComponent.Builder
    ): ViewModelProvider.Factory {
        return ViewModelsFactory(viewModelSubComponent.build())
    }
}