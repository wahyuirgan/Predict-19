package academy.bangkit.predict19.di

import academy.bangkit.predict19.ui.account.AccountViewModel
import academy.bangkit.predict19.ui.help.HelpViewModel
import academy.bangkit.predict19.ui.home.HomeViewModel
import academy.bangkit.predict19.ui.prediction.PredictionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object Koin {
    val appModule = module {
        viewModel { HomeViewModel() }
        viewModel { PredictionViewModel() }
        viewModel { HelpViewModel() }
        viewModel { AccountViewModel() }
    }
}
