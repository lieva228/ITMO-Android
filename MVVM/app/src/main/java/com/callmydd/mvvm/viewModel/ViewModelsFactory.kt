package com.callmydd.mvvm.viewModel

import androidx.collection.ArrayMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.callmydd.mvvm.di.ViewModelSubComponent
import java.util.concurrent.Callable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewModelsFactory @Inject constructor(private val viewModelSubComponent: ViewModelSubComponent) : ViewModelProvider.Factory {
    private val creators: ArrayMap<Class<*>, Callable<out ViewModel>> = ArrayMap()

    init {
        creators[MainViewModel::class.java] =
            Callable<ViewModel> { viewModelSubComponent.mainViewModel() }
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModelProvider = creators[modelClass]
            ?: throw IllegalArgumentException(
                "model class "
                        + modelClass
                        + " not found"
            )
        return try {
            viewModelProvider.call() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}