package academy.bangkit.predict19

import academy.bangkit.predict19.di.Koin.appModule
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PredictApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PredictApp)
            modules(appModule)
        }
    }

}