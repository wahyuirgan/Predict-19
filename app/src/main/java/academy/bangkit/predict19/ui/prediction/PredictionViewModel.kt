package academy.bangkit.predict19.ui.prediction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PredictionViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is prediction Fragment"
    }
    val text: LiveData<String> = _text
}