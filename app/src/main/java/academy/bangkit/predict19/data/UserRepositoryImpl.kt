package academy.bangkit.predict19.data

import academy.bangkit.predict19.extension.await
import academy.bangkit.predict19.util.Result
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.Exception

@ExperimentalCoroutinesApi
class UserRepositoryImpl : UserRepository {

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val userCollection = firestoreInstance.collection("users")
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun registerUser(
        email: String,
        password: String,
        context: Context
    ): Result<FirebaseUser?> {
        try {
            return when(val resultDocumentSnapshot = firebaseAuth.createUserWithEmailAndPassword(email, password).await()) {
                is Result.Success -> {
                    val firebaseUser = resultDocumentSnapshot.data.user
                    Result.Success(firebaseUser)
                }
                is Result.Error -> {
                    Result.Error(resultDocumentSnapshot.exception)
                }
                is Result.Cancel -> {
                    Result.Cancel(resultDocumentSnapshot.exception)
                }
            }
        } catch (exception: Exception) {
            return Result.Error(exception)
        }
    }

    override suspend fun createUserFirebase(user: User): Result<Void?> {
        return try {
            userCollection.document(user.id).set(user).await()
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }
}