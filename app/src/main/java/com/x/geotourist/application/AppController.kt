package com.x.geotourist.application

import android.app.Activity
import android.app.Application
import android.app.Service
import androidx.fragment.app.Fragment
import com.x.geotourist.di.appComponent.DaggerAppComponent
import com.x.geotourist.utils.Utils
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject


class AppController : Application(), HasActivityInjector, HasSupportFragmentInjector,
    HasServiceInjector {

    companion object {
        private lateinit var mInstance: AppController

        /*returns Application object or application context
     * */
        @Synchronized
        @JvmStatic
        fun getInstance(): AppController {
            return mInstance
        }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun activityInjector(): DispatchingAndroidInjector<Activity>? {
        return dispatchingAndroidInjector
    }


    /*Inject android fragments @see @link{#FragmentModule} for list of injected fragments*/
    @Inject
    lateinit var dispatchingFragmentAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment>? {
        return dispatchingFragmentAndroidInjector
    }


    /*Inject android services @see @link{#ServiceModule} for list of injected services*/
    @Inject
    lateinit var dispatchingServiceAndroidInjector: DispatchingAndroidInjector<Service?>

    override fun serviceInjector(): DispatchingAndroidInjector<Service?>? {
        return dispatchingServiceAndroidInjector
    }


    override fun onCreate() {
        super.onCreate()
        mInstance = this@AppController
        Utils.createNotificationChannel()
        inject()

        if (com.x.geotourist.BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    public fun inject() {
        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)
    }


}
