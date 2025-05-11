package com.example.anwerandquestion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.anwerandquestion.databinding.ActivityVerificationBinding
import com.google.firebase.auth.FirebaseAuth

class VerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerificationBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    companion object {
        const val MOBILE_NUMBER_KEY = "mobileNumber"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (auth.currentUser != null) {
            startActivity(Intent(this@VerificationActivity, MainActivity::class.java))
            finish()
        }

        supportActionBar?.hide()
        binding.NumberET.requestFocus()

        binding.btnContinue.setOnClickListener {
            val mobileNumber = binding.NumberET.text.toString()
            if (mobileNumber.isNotBlank() && mobileNumber.length == 13) {
                val intent = Intent(this@VerificationActivity, OTP_Activity::class.java)
                intent.putExtra(MOBILE_NUMBER_KEY, mobileNumber)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
