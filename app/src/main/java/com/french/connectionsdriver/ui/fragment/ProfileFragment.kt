package com.french.connectionsdriver.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.french.connectionsdriver.R
import com.french.connectionsdriver.databinding.FragmentProfileBinding
import com.french.connectionsdriver.ui.activity.DriverInfoActivity
import com.french.connectionsdriver.ui.activity.EditProfileActivity
import com.french.connectionsdriver.ui.activity.NotificationActivity
import com.french.connectionsdriver.ui.util.UserSession
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private val binding: FragmentProfileBinding by lazy {
        FragmentProfileBinding.inflate(layoutInflater)
    }

    private lateinit var fragmentContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setListener()

        return binding.root
    }

    private fun setListener() {

        binding.apply {

            nameTV.text = UserSession.user.userName
            if (UserSession.user.image.equals("")) {
                profileIV.setImageResource(R.drawable.chat_dummy)
            } else {
                Glide.with(requireActivity()).load(UserSession.user.image).into(profileIV)
            }
            accountLayout.setOnClickListener {
                startActivity(Intent(fragmentContext, EditProfileActivity::class.java))
                (fragmentContext as Activity).overridePendingTransition(
                    androidx.appcompat.R.anim.abc_fade_in,
                    androidx.appcompat.R.anim.abc_fade_out
                )
            }

            notificationLayout.setOnClickListener {
                startActivity(Intent(fragmentContext, NotificationActivity::class.java))
                (fragmentContext as Activity).overridePendingTransition(
                    androidx.appcompat.R.anim.abc_fade_in,
                    androidx.appcompat.R.anim.abc_fade_out
                )
            }

            driverLayout.setOnClickListener {
                startActivity(Intent(fragmentContext, DriverInfoActivity::class.java))
                (fragmentContext as Activity).overridePendingTransition(
                    androidx.appcompat.R.anim.abc_fade_in,
                    androidx.appcompat.R.anim.abc_fade_out
                )
            }

            deleteLayout.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                val user = Firebase.auth.currentUser!!

                user.delete().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            progressBar.visibility = View.GONE
                           requireActivity().finish()
                        }else{
                            progressBar.visibility = View.GONE
                            Log.d("Logger", "setListener: ${task.exception!!.message}")
                            Toast.makeText(fragmentContext, "${task.exception!!.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }
}