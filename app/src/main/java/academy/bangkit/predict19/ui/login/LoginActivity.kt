package academy.bangkit.predict19.ui.login

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

    private lateinit var binding: ActivityLoginBinding

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            binding.btnLogin.isEnabled = false
            binding.btnGoogleLogin.isEnabled = false
            binding.tvSignup.isEnabled = false
            binding.mainProgressbarLogin.visibility = View.VISIBLE

            val email: String = binding.etEmailLogin.text.toString()
            val password: String = binding.etPassLogin.text.toString()

            val emailPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")

            when {
                email.isEmpty() -> {
                    binding.etEmailLogin.error = "Email Cannot Empty"
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogleLogin.isEnabled = true
                    binding.tvSignup.isEnabled = true
                    binding.mainProgressbarLogin.visibility = View.GONE
                }
                !emailPattern.matcher(email).matches() -> {
                    binding.etEmailLogin.error = "Invalid Email Format"
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogleLogin.isEnabled = true
                    binding.tvSignup.isEnabled = true
                    binding.mainProgressbarLogin.visibility = View.GONE
                }
                password.isEmpty() -> {
                    binding.etPassLogin.error = "Password Cannot Empty"
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogleLogin.isEnabled = true
                    binding.tvSignup.isEnabled = true
                    binding.mainProgressbarLogin.visibility = View.GONE
                }
                password.length < 8 -> {
                    binding.etPassLogin.error = "Password Must at Least 8 Character"
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogleLogin.isEnabled = true
                    binding.tvSignup.isEnabled = true
                    binding.mainProgressbarLogin.visibility = View.GONE
                }
                else -> {
                    loginProcess(email, password)
                }
            }
        }

        binding.tvSignup.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loginProcess(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
            this) { task ->
            if (task.isSuccessful) {
                checkEmailVerification()
                binding.btnLogin.isEnabled = true
                binding.btnGoogleLogin.isEnabled = true
                binding.tvSignup.isEnabled = true
                binding.mainProgressbarLogin.visibility = View.GONE
                finishAffinity()
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    task.exception?.message,
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnLogin.isEnabled = true
                binding.btnGoogleLogin.isEnabled = true
                binding.tvSignup.isEnabled = true
                binding.mainProgressbarLogin.visibility = View.GONE
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
            Toast.makeText(this, "Verifikasi Email Kamu", Toast.LENGTH_SHORT).show()
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

}
