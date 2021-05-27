package academy.bangkit.predict19.data

import academy.bangkit.predict19.util.Result
import android.content.Context
import com.google.firebase.auth.FirebaseUser

interface UserRepository {
    suspend fun registerUser(email: String, password: String, context: Context) : Result<FirebaseUser?>
    suspend fun createUserFirebase(user: User) : Result<Void?>
}