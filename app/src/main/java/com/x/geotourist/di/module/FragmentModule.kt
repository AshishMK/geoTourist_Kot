package com.x.geotourist.di.module

import com.x.geotourist.scenes.mainScene.TourListFragment
import com.x.geotourist.scenes.mapScene.MapFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/*
 *  Module to inject specified list of fragments
 */
@Module
public abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeTourListFragment(): TourListFragment
    @ContributesAndroidInjector
    abstract fun contributeMapFragment(): MapFragment
}