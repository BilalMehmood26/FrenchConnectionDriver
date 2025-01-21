package com.french.connectionsdriver.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.french.connectionsdriver.R
import com.french.connectionsdriver.ui.activity.DashboardActivity
import com.french.connectionsdriver.ui.activity.LoginActivity
import com.french.connectionsdriver.ui.model.User
import com.french.connectionsdriver.ui.util.UserSession
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            val userId = auth.currentUser!!.uid
            db.collection("Users").document(userId)
                .get().addOnSuccessListener { task ->
                    val user = task.toObject(User::class.java)
                    user!!.id = userId
                    UserSession.user = user
                    val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                    intent.putExtra("user","user")
                    startActivity(intent)
                    finish()
                    overridePendingTransition(
                        androidx.appcompat.R.anim.abc_fade_in,
                        androidx.appcompat.R.anim.abc_fade_out
                    )
                }.addOnFailureListener {
                    Handler().postDelayed(Runnable {
                        startActivity(Intent(this, LoginActivity::class.java))
                        overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                        finish()
                    }, 2000)
                }
        }else{
            Handler().postDelayed(Runnable {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                finish()
            }, 2000)
        }
    }
}