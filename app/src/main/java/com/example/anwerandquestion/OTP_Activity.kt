package com.example.anwerandquestion

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.anwerandquestion.databinding.ActivityOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class OTP_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    private var verificationId: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dialog = ProgressDialog(this).apply {
            setMessage("Sending OTP...")
            setCancelable(false)
            show()
        }

        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()

        val mobileNumber = intent.getStringExtra("mobileNumber")
        if (mobileNumber.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid mobile number.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.phoneLabel.text = "Verify $mobileNumber"

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(mobileNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    dialog.dismiss()
                    Toast.makeText(this@OTP_Activity, "Verification failed: ${exception.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verifyId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    dialog.dismiss()
                    verificationId = verifyId
                    binding.otpView.requestFocus()
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        binding.otpView.setOtpCompletionListener { otp ->
            verificationId?.let {
                val credential = PhoneAuthProvider.getCredential(it, otp)
                signInWithCredential(credential)
            } ?: Toast.makeText(this, "Verification ID not found. Retry.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkUserProfile()
                } else {
                    Toast.makeText(this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val userReference = FirebaseDatabase.getInstance().getReference("users").child(uid)
            userReference.get().addOnCompleteListener { task ->
                if (task.isSuccessful && task.result.exists()) {
                    // User profile already exists, go to main activity
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // No user profile, go to SetupProfileActivity
                    startActivity(Intent(this, SetupProfileActivity::class.java))
                }
                finishAffinity()
            }
        }
    }
}
