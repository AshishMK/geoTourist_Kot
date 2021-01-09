package com.x.geotourist.di.module

import com.x.geotourist.scenes.playerScene.PlayerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/*
 *  Module to inject specified list of activities
 */
@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun contributePlayerActivity(): PlayerActivity

}