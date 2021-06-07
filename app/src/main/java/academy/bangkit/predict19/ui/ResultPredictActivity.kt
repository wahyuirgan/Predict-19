package academy.bangkit.predict19.ui

import academy.bangkit.predict19.R
import academy.bangkit.predict19.databinding.ActivityResultPredictBinding
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*


class ResultPredictActivity : AppCompatActivity() {

    private var _binding: ActivityResultPredictBinding? = null
    private val binding get() = _binding

    private lateinit var tfLite: Interpreter
    private lateinit var inputImageBuffer: TensorImage
    private var imageSizeX = 0
    private var imageSizeY = 0
    private lateinit var outputProbabilityBuffer: TensorBuffer
    private lateinit var probabilityProcessor: TensorProcessor
    private val imageMean = 0.0f
    private val imageStd = 1.0f
    private val probabilityMean = 0.0f
    private val probabilityStd = 255.0f
    private var labels: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityResultPredictBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbar)
        supportActionBar?.title = getString(R.string.title_prediction)
        supportActionBar?.setLogo(R.drawable.ic_logo_predict_icon)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        try {
            tfLite = Interpreter(loadModelFile(this))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        predict()

//        predictImage()

    }

//    private fun predictImage() {
//        val uri: Uri? = intent.getParcelableExtra("resId")
//        var bitmapImage = MediaStore.Images.Media.getBitmap(
//            applicationContext?.contentResolver, uri
//        )
//        bitmapImage = Bitmap.createScaledBitmap(bitmapImage,
//            224,
//            224, false)
//        binding?.ivImagePredict?.setImageBitmap(bitmapImage)
//    }

    private fun predict() {
        val imageTensorIndex = 0
        val imageShape = tfLite.getInputTensor(imageTensorIndex).shape() // {1, height, width, 3}

        imageSizeY = imageShape[1]
        imageSizeX = imageShape[2]
        val imageDataType: DataType? = tfLite.getInputTensor(imageTensorIndex)?.dataType()

        val probabilityTensorIndex = 0
        val probabilityShape =
            tfLite.getOutputTensor(probabilityTensorIndex).shape() // {1, NUM_CLASSES}

        val probabilityDataType: DataType? =
            tfLite.getOutputTensor(probabilityTensorIndex).dataType()

        inputImageBuffer = TensorImage(imageDataType)
        outputProbabilityBuffer =
            TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)
        probabilityProcessor = TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build()

        val uri: Uri? = intent.getParcelableExtra("resId")
        var bitmap = MediaStore.Images.Media.getBitmap(
            applicationContext?.contentResolver, uri
        )
        bitmap = Bitmap.createScaledBitmap(bitmap,
            224,
            224, false)

        binding?.ivImagePredict?.setImageBitmap(bitmap)
        inputImageBuffer = loadImage(bitmap)

        tfLite.run(inputImageBuffer.buffer, outputProbabilityBuffer.buffer.rewind())
        showResult()
    }

    private fun loadImage(bitmap: Bitmap): TensorImage {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap)

        // Creates processor for the TensorImage.
        val cropSize = bitmap.width.coerceAtMost(bitmap.height)

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(getPreprocessNormalizeOp())
            .build()
        return imageProcessor.process(inputImageBuffer)
    }

    private fun loadModelFile(activity: Activity): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd("pred_mobile.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun getPreprocessNormalizeOp(): TensorOperator {
        return NormalizeOp(imageMean, imageStd)
    }

    private fun getPostprocessNormalizeOp(): TensorOperator {
        return NormalizeOp(probabilityMean, probabilityStd)
    }

    private fun showResult() {
        try {
            labels = FileUtil.loadLabels(this, "labels_predict.txt")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val labeledProbability =
            labels?.let {
                TensorLabel(it, probabilityProcessor.process(outputProbabilityBuffer))
                    .mapWithFloatValue
            }
        val maxValueInMap: Float = Collections.max(labeledProbability?.values)
        if (labeledProbability != null) {
            for ((key, value) in labeledProbability) {
                if (value == maxValueInMap) {
                    binding?.tvPredictResult?.text = key
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}