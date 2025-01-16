package com.example.frenchconnectiondriver.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frenchconnectiondriver.R
import com.example.frenchconnectiondriver.databinding.ActivityConversationBinding
import com.example.frenchconnectiondriver.ui.adapter.ConversationAdapter
import com.example.frenchconnectiondriver.ui.model.Conversation
import com.example.frenchconnectiondriver.ui.util.UserSession
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class ConversationActivity : AppCompatActivity() {

    private val binding: ActivityConversationBinding by lazy {
        ActivityConversationBinding.inflate(layoutInflater)
    }

    private var conversationModelList: ArrayList<Conversation> = ArrayList()
    private var toID = ""
    private var messageId = ""
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        toID = intent.getStringExtra("userID")!!
        messageId = intent.getStringExtra("messageId")!!
        setListener()
        getChat()

        binding.sendBtn.setOnClickListener {
            if (!binding.messageTv.text.isEmpty()) {
                sendMessage(binding.messageTv.text.toString())
            }
        }
    }


    private fun setAdapter() {
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        binding.recyclerView.adapter = ConversationAdapter(this, conversationModelList)
    }

    private fun setListener() {
        binding.backIV.setOnClickListener { onBackPressed() }
    }

    private fun getChat() {
        binding.progressBar.visibility = View.VISIBLE
        if (messageId.isEmpty()) {
            db.collection("Chat").document(messageId).get().addOnCompleteListener {

            }
            /* db.collection("Chat").get().addOnSuccessListener { queryDocumentSnapshots ->
                 for (document in queryDocumentSnapshots) {
                     binding.progressBar.visibility = View.GONE
                     val participantMap = document.get("participants") as Map<String, Any>?

                     val hasCurrentUser = participantMap?.containsKey(UserSession.user.id) == true
                     val hasOpponent = participantMap?.containsKey(toID) == true

                     if (hasCurrentUser && hasOpponent) {
                         val documentId = document.id
                         toID = documentId
                         Log.d("LOGGER", "ID: $documentId")
                         break
                     }
                 }
                 addRealtimeListener()
             }*/
        } else {
            addRealtimeListener()
        }
    }

    private fun addRealtimeListener() {
        if (!messageId.isNullOrEmpty()) {
            FirebaseFirestore.getInstance().collection("Chat")
                .document(messageId).collection("Conversation")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { queryDocumentSnapshots, e ->
                    if (e != null) {
                        // Handle the error
                        Toast.makeText(this@ConversationActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        return@addSnapshotListener
                    }

                    conversationModelList.clear()
                    binding.progressBar.visibility = View.GONE
                    for (documentSnapshot in queryDocumentSnapshots ?: emptyList()) {
                        val model = documentSnapshot.toObject(Conversation::class.java)
                        if(model.content.isNotEmpty()){
                            conversationModelList.add(model)
                        }
                    }
                    setAdapter()
                }
        }
    }

    private fun sendMessage(message: String) {
        val id = UUID.randomUUID().toString()
        val timeStamp = System.currentTimeMillis()

        val messageMap = hashMapOf(
            "content" to message,
            "fromID" to Firebase.auth.currentUser!!.uid,
            "toID" to toID,
            "messageId" to messageId,
            "read" to false,
            "timestamp" to timeStamp,
            "type" to "text"
        )

        val participents = hashMapOf(
            UserSession.user.id to true,
            toID to true
        )


        val lastMessageMap = hashMapOf(
            "lastMessage" to messageMap,
            "participants" to participents,
            "chatType" to "one"
        )

        FirebaseFirestore.getInstance().collection("Chat").document(messageId)
            .update(lastMessageMap as Map<String, Any>)
        FirebaseFirestore.getInstance().collection("Chat").document(messageId)
            .collection("Conversation").document(id).set(messageMap)
        binding.messageTv.setText("")

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            androidx.appcompat.R.anim.abc_fade_in,
            androidx.appcompat.R.anim.abc_fade_out
        )
    }
}