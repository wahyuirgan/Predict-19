package academy.bangkit.predict19.ui.login

import academy.bangkit.predict19.R
import academy.bangkit.predict19.databinding.ActivityLoginBinding
import academy.bangkit.predict19.ui.MainActivity
import academy.bangkit.predict19.ui.RegisterActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnLogin?.setOnClickListener {
            binding?.btnLogin?.isEnabled = false
            binding?.btnGoogleLogin?.isEnabled = false
            binding?.tvSignup?.isEnabled = false
            binding?.mainProgressbarLogin?.visibility = View.VISIBLE

            val email: String = binding?.etEmailLogin?.text.toString()
            val password: String = binding?.etPassLogin?.text.toString()

            val emailPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")

            when {
                email.isEmpty() -> {
                    binding?.etEmailLogin?.error = getString(R.string.notif_email_empty)
                    binding?.btnLogin?.isEnabled = true
                    binding?.btnGoogleLogin?.isEnabled = true
                    binding?.tvSignup?.isEnabled = true
                    binding?.mainProgressbarLogin?.visibility = View.GONE
                }
                !emailPattern.matcher(email).matches() -> {
                    binding?.etEmailLogin?.error = getString(R.string.notif_email_invalid)
                    binding?.btnLogin?.isEnabled = true
                    binding?.btnGoogleLogin?.isEnabled = true
                    binding?.tvSignup?.isEnabled = true
                    binding?.mainProgressbarLogin?.visibility = View.GONE
                }
                password.isEmpty() -> {
                    binding?.etPassLogin?.error = getString(R.string.notif_pass_empty)
                    binding?.btnLogin?.isEnabled = true
                    binding?.btnGoogleLogin?.isEnabled = true
                    binding?.tvSignup?.isEnabled = true
                    binding?.mainProgressbarLogin?.visibility = View.GONE
                }
                password.length < 8 -> {
                    binding?.etPassLogin?.error = getString(R.string.notif_pass_less_than_eight)
                    binding?.btnLogin?.isEnabled = true
                    binding?.btnGoogleLogin?.isEnabled = true
                    binding?.tvSignup?.isEnabled = true
                    binding?.mainProgressbarLogin?.visibility = View.GONE
                }
                else -> {
                    loginProcess(email, password)
                }
            }
        }

        binding?.tvSignup?.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loginProcess(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
            this) { task ->
            if (task.isSuccessful) {
                checkEmailVerification()
                binding?.btnLogin?.isEnabled = true
                binding?.btnGoogleLogin?.isEnabled = true
                binding?.tvSignup?.isEnabled = true
                binding?.mainProgressbarLogin?.visibility = View.GONE
                finishAffinity()
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    task.exception?.message,
                    Toast.LENGTH_SHORT
                ).show()
                binding?.btnLogin?.isEnabled = true
                binding?.btnGoogleLogin?.isEnabled = true
                binding?.tvSignup?.isEnabled = true
                binding?.mainProgressbarLogin?.visibility = View.GONE
            }
        }
    }

    private fun checkEmailVerification() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val emailFlag = firebaseUser?.isEmailVerified
        val mainActivity = Intent(applicationContext, MainActivity::class.java)
        startActivity(mainActivity)
        if (emailFlag == true) {
            finish()
            startActivity(mainActivity)
        } else {
            val loginActivity = Intent(applicationContext, LoginActivity::class.java)
            startActivity(loginActivity)
            Toast.makeText(this, getString(R.string.notif_verify_email), Toast.LENGTH_SHORT).show()
            firebaseAuth.signOut()
        }
    }

    override fun onStart() {
        super.onStart()
        val user = firebaseAuth.currentUser
        if (user != null) {
            val mainActivity = Intent(applicationContext, MainActivity::class.java)
            startActivity(mainActivity)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
