package com.example.anwerandquestion

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.anwerandquestion.databinding.ActivitySetupProfileBinding
import com.example.anwerandquestion.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class SetupProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupProfileBinding
    private var auth: FirebaseAuth? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var selectedImage: Uri? = null
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initializing views and Firebase components
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initializing ProgressDialog
        dialog = ProgressDialog(this).apply {
            setMessage("Updating Profile...")
            setCancelable(false)
        }

        // Hiding action bar
        supportActionBar?.hide()

        // Edge-to-edge and insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up profile image selection
        binding.imgUser.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, 45)
        }

        // Handle button setup click
        binding.btnSetup.setOnClickListener {
            val name: String = binding.NameET.text.toString()
            if (name.isEmpty()) {
                binding.NameET.error = "Please type name"
                return@setOnClickListener
            }

            dialog?.show()
            if (selectedImage != null) {
                val reference = storage!!.reference.child("Profile").child(auth!!.uid!!)
                reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnCompleteListener { uriTask ->
                            if (uriTask.isSuccessful) {
                                val imageUrl = uriTask.result.toString()
                                saveUserToDatabase(name, imageUrl)
                            }
                        }
                    } else {
                        saveUserToDatabase(name, "No Image")
                    }
                }
            } else {
                saveUserToDatabase(name, "No Image")
            }
        }
    }

    private fun saveUserToDatabase(name: String, imageUrl: String) {
        val uid = auth!!.uid
        val mobile = auth!!.currentUser?.phoneNumber
        val user = User(uid, name, mobile, imageUrl)

        database!!.reference
            .child("users")
            .child(uid!!)
            .setValue(user)
            .addOnCompleteListener {
                dialog?.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 45 && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImage = data.data
            binding.imgUser.setImageURI(selectedImage)

            // Upload selected image directly
            val time = Date().time
            val reference = storage!!.reference
                .child("Profile")
                .child("${time}")

            reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    reference.downloadUrl.addOnCompleteListener { uriTask ->
                        if (uriTask.isSuccessful) {
                            val filePath = uriTask.result.toString()
                            val obj = hashMapOf<String, Any>("image" to filePath)
                            database!!.reference
                                .child("users")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj)
                        }
                    }
                }
            }
        }
    }
}
