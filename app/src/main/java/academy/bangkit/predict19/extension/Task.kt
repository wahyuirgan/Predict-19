package academy.bangkit.predict19.extension

import academy.bangkit.predict19.util.Result
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume

@ExperimentalCoroutinesApi
suspend fun <T> Task<T>.await(): Result<T>
{
    if (isComplete)
    {
        val e = exception
        return if (e == null)
        {
            if (isCanceled)
            {
                Result.Cancel(CancellationException("Task $this was cancelled normally."))
            }
            else
            {
                @Suppress("UNCHECKED_CAST")
                Result.Success(result as T)
            }
        }
        else
        {
            Result.Error(e)
        }
    }

    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener {
            val e = exception
            if (e == null)
            {
                if (isCanceled)
                {
                    cont.cancel()
                }
                else
                {
                    @Suppress("UNCHECKED_CAST")
                    cont.resume(Result.Success(result as T))
                }
            }
            else
            {
                cont.resumeWithException(e)
            }
        }
    }
}