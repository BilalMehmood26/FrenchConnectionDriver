package com.french.connectionsdriver.ui.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.french.connectionsdriver.R
import com.french.connectionsdriver.databinding.ItemDesignChatLayoutBinding
import com.french.connectionsdriver.ui.activity.ConversationActivity
import com.french.connectionsdriver.ui.model.ChatModel
import com.french.connectionsdriver.ui.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatAdapter(
    val context: Context,
    private val list: ArrayList<ChatModel>
) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val db = Firebase.firestore

    inner class ViewHolder(val binding: ItemDesignChatLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDesignChatLayoutBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {

            if (item.driverID!!.isNotEmpty()) {
                db.collection("Users").document(item.driverID!!).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result.toObject(User::class.java)
                        userNameTv.text = user!!.userName
                        if (user.image!!.isNotEmpty()) {
                            Glide.with(context).load(user.image).into(profileIV)
                        } else {
                            profileIV.setImageResource(R.drawable.main_logo)
                        }
                    } else {
                        Log.d("LOGGER", "is Fail")
                        Toast.makeText(
                            context,
                            task.exception!!.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            root.setOnClickListener {

                val intent = Intent(context, ConversationActivity::class.java)
                intent.putExtra("userID",item.userID)
                intent.putExtra("messageId",item.bookingID)
                context.startActivity(intent)
                (context as Activity).overridePendingTransition(
                    androidx.appcompat.R.anim.abc_fade_in,
                    androidx.appcompat.R.anim.abc_fade_out
                )
            }
        }
    }
}