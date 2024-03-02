package com.example.entregadores

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o Firebase Auth
        auth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val user = auth.currentUser
            if (user != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // Usuário não está logado, vai para LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
