package com.x.geotourist.di.module

import androidx.lifecycle.ViewModelProvider
import com.x.geotourist.di.factory.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
internal abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

}