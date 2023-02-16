package com.callmydd.mvvm.di;

import com.callmydd.mvvm.viewModel.MainViewModel;
import dagger.Subcomponent;

@Subcomponent
public interface ViewModelSubComponent {
    @Subcomponent.Builder
    interface Builder {
        ViewModelSubComponent build();
    }

    MainViewModel mainViewModel();
}
