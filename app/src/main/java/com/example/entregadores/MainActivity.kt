package com.example.entregadores

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val tvUserPhone = findViewById<TextView>(R.id.tvUserPhone)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val user = auth.currentUser
        tvUserPhone.text = user?.phoneNumber ?: "Telefone não disponível"

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
             finish()
        }
    }
}
