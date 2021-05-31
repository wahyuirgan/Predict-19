package academy.bangkit.predict19.ui.home

import academy.bangkit.predict19.R
import academy.bangkit.predict19.data.User
import academy.bangkit.predict19.databinding.FragmentHomeBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding?.tvWelcomeName?.text = getString(R.string.label_greeting).plus(" ").plus(firebaseUser?.displayName)
        binding?.tvNameHome?.text = firebaseUser?.displayName

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("users").child(firebaseAuth.uid.toString()).addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    val dateBirth: String? = user?.dateBirth
                    val changeDateBirth = dateBirth?.let { getAge(it) }

                    binding?.tvAgeHome?.text = changeDateBirth.toString().plus(" ").plus(getString(R.string.label_age_home))
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        context,
                        getString(R.string.notif_user_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        return binding?.root
    }

    private fun getAge(
        dateString: String
    ): Int {
        val dateFormatter = SimpleDateFormat("dd MMM yyy", Locale.US)
        var date: Date? = null
        try {
            date = dateFormatter.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        if (date == null) {
            return 0
        }

        val dob: Calendar = Calendar.getInstance()
        val today: Calendar = Calendar.getInstance()

        dob.time = date

        val year: Int = dob.get(Calendar.YEAR)
        val month: Int = dob.get(Calendar.MONTH)
        val day: Int = dob.get(Calendar.DAY_OF_MONTH)

        dob.set(year, month+1, day)

        var age: Int = today.get(Calendar.YEAR) - year

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}