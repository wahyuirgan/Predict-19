package academy.bangkit.predict19.ui

import academy.bangkit.predict19.BuildConfig
import academy.bangkit.predict19.R
import academy.bangkit.predict19.databinding.ActivitySplashScreenBinding
import academy.bangkit.predict19.ui.login.LoginActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashScreenActivity : AppCompatActivity() {

    private var _binding: ActivitySplashScreenBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.tvVersionApp?.text = getString(R.string.label_version).plus(" ").plus(BuildConfig.VERSION_NAME)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }, 6000)
    }
}