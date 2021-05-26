package academy.bangkit.predict19.util

import java.lang.Exception


sealed class Result<out R> {

    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data class Cancel(val exception: Exception?) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            is Cancel -> "Cancel[exception=$exception]"
        }
    }
}
