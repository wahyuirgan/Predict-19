package academy.bangkit.predict19.ui.login

import academy.bangkit.predict19.R
import academy.bangkit.predict19.data.User
import academy.bangkit.predict19.data.UserRepository
import academy.bangkit.predict19.ui.MainActivity
import academy.bangkit.predict19.ui.RegisterActivity
import academy.bangkit.predict19.util.Result
import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginViewModel(var userRepository: UserRepository) : ViewModel() {

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?>
    get() = _toast

    private val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean>
    get() = _spinner

    private val _currentUser = MutableLiveData(User())
    val currentUser: LiveData<User>
    get() = _currentUser

    fun registerUser(name: String, email: String, password: String, activity: Activity)
    {
        launchDataLoad {
            viewModelScope.launch {
                when(val result = userRepository.registerUser(email, password, activity.applicationContext))
                {
                    is Result.Success -> {
                        result.data?.let {firebaseUser ->
                            createUserFirebase(createUserObject(firebaseUser, name), activity)
                        }
                    }
                    is Result.Error -> {
                        _toast.value = result.exception.message
                    }
                    is Result.Cancel -> {
                        _toast.value = activity.getString(R.string.request_canceled)
                    }
                }
            }
        }
    }
    private suspend fun createUserFirebase(user: User, activity: Activity)
    {
        when(val result = userRepository.createUserFirebase(user))
        {
            is Result.Success -> {
                when(activity)
                {
                    is RegisterActivity -> {
                        _toast.value = activity.getString(R.string.registration_successful)
                    }
                    is LoginActivity -> {
                        _toast.value = activity.getString(R.string.login_successful)
                    }
                }
                _currentUser.value = user
                startActivitiy(activity)
            }
            is Result.Error -> {
                _toast.value = result.exception.message
            }
            is Result.Cancel -> {
                _toast.value = activity.getString(R.string.request_canceled)
            }
        }
    }


    fun createUserObject(firebaseUser: FirebaseUser, name: String, userImage: String = ""): User {

        return User(
            id = firebaseUser.uid,
            name = name,
            userImage = userImage
        )
    }

    private fun launchDataLoad(block: suspend () -> Unit): Job
    {
        return viewModelScope.launch {
            try
            {
                _spinner.value = true
                block()
            }
            catch (error: Throwable)
            {
                _toast.value = error.message
            }
            finally
            {
                _spinner.value = false
            }
        }
    }
    private fun startActivitiy(activity: Activity)
    {
        val intent = Intent(activity, MainActivity::class.java)
        activity.startActivity(intent)
    }

}