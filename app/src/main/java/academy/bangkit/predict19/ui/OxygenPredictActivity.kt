package academy.bangkit.predict19.ui

import academy.bangkit.predict19.R
import academy.bangkit.predict19.databinding.ActivityOxygenPredictBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.*

class OxygenPredictActivity : AppCompatActivity() {

    private var _binding: ActivityOxygenPredictBinding? = null
    private val binding get() = _binding

    private lateinit var resultOxygenFirstTier: Array<String>
    private lateinit var resultOxygenSecondTier: Array<String>
    private lateinit var resultOxygenThirdTier: Array<String>
    private lateinit var resultOxygenFourthTier: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOxygenPredictBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbar)
        supportActionBar?.title = getString(R.string.title_prediction)
        supportActionBar?.setLogo(R.drawable.ic_logo_predict_icon)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        resultOxygenFirstTier = resources.getStringArray(R.array.percent_first_tier_oxygen)
        resultOxygenSecondTier = resources.getStringArray(R.array.percent_second_tier_oxygen)
        resultOxygenThirdTier = resources.getStringArray(R.array.percent_third_tier_oxygen)
        resultOxygenFourthTier = resources.getStringArray(R.array.percent_fourth_tier_oxygen)

        oxygenPredict()

    }

    private fun oxygenPredict() {

        val randomOxygenFirstTierIndex: Int = resultOxygenFirstTier.size.let { Random().nextInt(it) }
        val randomOxygenSecondTierIndex: Int = resultOxygenSecondTier.size.let { Random().nextInt(it) }
        val randomOxygenThirdTierIndex: Int = resultOxygenThirdTier.size.let { Random().nextInt(it) }
        val randomOxygenFourthTierIndex: Int = resultOxygenFourthTier.size.let { Random().nextInt(it) }

        when (intent.getStringExtra("predictResult")) {
            "covid" -> {
                binding?.layoutOxygenPredictFirstTier?.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))

                binding?.tvOxygenPercentFirstTier?.text = resultOxygenFourthTier[randomOxygenFourthTierIndex]
                binding?.tvOxygenPercentSecondTier?.text = resultOxygenThirdTier[randomOxygenThirdTierIndex]
                binding?.tvOxygenPercentThirdTier?.text = resultOxygenSecondTier[randomOxygenSecondTierIndex]
                binding?.tvOxygenPercentFourthTier?.text = resultOxygenFirstTier[randomOxygenFirstTierIndex]
            }
            "normal" -> {
                binding?.layoutOxygenPredictFourthTier?.setBackgroundColor(resources.getColor(R.color.colorHomePager))

                binding?.tvOxygenPercentFirstTier?.text = resultOxygenFirstTier[randomOxygenFirstTierIndex]
                binding?.tvOxygenPercentSecondTier?.text = resultOxygenSecondTier[randomOxygenSecondTierIndex]
                binding?.tvOxygenPercentThirdTier?.text = resultOxygenThirdTier[randomOxygenThirdTierIndex]
                binding?.tvOxygenPercentFourthTier?.text = resultOxygenFourthTier[randomOxygenFourthTierIndex]
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}