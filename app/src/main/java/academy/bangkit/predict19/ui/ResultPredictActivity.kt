package academy.bangkit.predict19.ui

import academy.bangkit.predict19.R
import academy.bangkit.predict19.databinding.ActivityResultPredictBinding
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class ResultPredictActivity : AppCompatActivity() {

    private var _binding: ActivityResultPredictBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityResultPredictBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbar)
        supportActionBar?.title = getString(R.string.title_prediction)
        supportActionBar?.setLogo(R.drawable.ic_logo_predict_icon)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        predictImage()
        binding?.btnAnalysisPredict?.setOnClickListener {
            oxygenPredict()
        }

    }

    private fun oxygenPredict() {

        binding?.tvPredictResultServer?.text = intent.getStringExtra("predictResult")

        val predictResult: String = binding?.tvPredictResultServer?.text.toString()

        val intent = Intent(this@ResultPredictActivity, OxygenPredictActivity::class.java)
        intent.putExtra("predictResult", predictResult)
        startActivity(intent)
    }

    private fun predictImage() {
        val uri: Uri? = intent.getParcelableExtra("resId")
        var bitmapImage = MediaStore.Images.Media.getBitmap(
            applicationContext?.contentResolver, uri
        )
        bitmapImage = Bitmap.createScaledBitmap(bitmapImage,
            224,
            224, false)
        binding?.ivImagePredict?.setImageBitmap(bitmapImage)

        when (intent?.getStringExtra("predictResult")) {
            "covid" -> {
                binding?.tvPredictResult?.text = Html.fromHtml(getString(R.string.text_positif))
                binding?.btnAnalysisPredict?.visibility = View.VISIBLE
            }
            "penumonia" -> {
                binding?.tvPredictResult?.text = getString(R.string.text_pneumonia)
                binding?.btnAnalysisPredict?.visibility = View.INVISIBLE
            }
            "normal" -> {
                binding?.tvPredictResult?.text = Html.fromHtml(getString(R.string.text_negatif))
                binding?.btnAnalysisPredict?.visibility = View.VISIBLE
            }
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}