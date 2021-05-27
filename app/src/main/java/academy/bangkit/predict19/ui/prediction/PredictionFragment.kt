package academy.bangkit.predict19.ui.prediction

import academy.bangkit.predict19.databinding.FragmentPredictionBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class PredictionFragment : Fragment() {

    private lateinit var predictionViewModel: PredictionViewModel
    private var _binding: FragmentPredictionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        predictionViewModel =
            ViewModelProvider(this).get(PredictionViewModel::class.java)

        _binding = FragmentPredictionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textFileSelected
        predictionViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}