package academy.bangkit.predict19.ui.account

import academy.bangkit.predict19.R
import academy.bangkit.predict19.data.User
import academy.bangkit.predict19.databinding.FragmentAccountBinding
import academy.bangkit.predict19.ui.login.LoginActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAccountBinding.inflate(inflater, container, false)

        context?.let {
            binding?.let { img ->
                Glide.with(it)
                    .load(firebaseUser?.photoUrl)
                    .placeholder(R.drawable.ic_profile_signup)
                    .error(android.R.color.holo_red_dark)
                    .into(img.ivProfileAccount)
            }
        }
        binding?.tvNameAccount?.text = firebaseUser?.displayName
        binding?.tvEmailAccount?.text = firebaseUser?.email

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("users").child(firebaseAuth.uid.toString()).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    val dateBirth: String? = user?.dateBirth

                    binding?.tvDateOfBirthAccount?.text = dateBirth
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

        binding?.btnLogout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}