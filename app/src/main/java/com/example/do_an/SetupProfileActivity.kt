package com.example.do_an

import android.app.ComponentCaller
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.do_an.databinding.ActivitySetupProfileBinding
import com.example.do_an.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding
    private var auth: FirebaseAuth? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var selectImg: Uri? = null
    private var dialog: ProgressDialog? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 45
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo ProgressDialog
        dialog = ProgressDialog(this)
        dialog?.setMessage("Updating Profile....")
        dialog?.setCancelable(false)

        // Khởi tạo Firebase
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()

        // Xử lý chọn ảnh
        binding.imgAvata.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        // Lưu thông tin người dùng
        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun saveUserProfile() {
        val name: String = binding.edtName.text.toString().trim()

        // Kiểm tra tên
        if (name.isEmpty()) {
            binding.edtName.error = "Please enter your name"
            binding.edtName.requestFocus()
            return
        }

        // Hiển thị dialog
        dialog?.show()

        val uid = auth?.uid
        val phone = auth?.currentUser?.phoneNumber

        // Kiểm tra uid và phone
        if (uid == null || phone == null) {
            showError("Authentication failed")
            return
        }

        // Xử lý upload ảnh và lưu thông tin người dùng
        if (selectImg != null) {
            uploadImageAndSaveUser(uid, name, phone)
        } else {
            saveUserWithoutImage(uid, name, phone)
        }
    }

    private fun uploadImageAndSaveUser(uid: String, name: String, phone: String?) {
        val reference = storage?.reference?.child("AvataUsers")?.child(uid)
        reference?.putFile(selectImg!!)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reference.downloadUrl.addOnCompleteListener { uriTask ->
                    val imageUrl = uriTask.result.toString()
                    val user = User(uid, name, phone, imageUrl)
                    saveUserToDatabase(user)
                }
            } else {
                saveUserWithoutImage(uid, name, phone)
            }
        }?.addOnFailureListener {
            showError("Image upload failed")
        }
    }

    private fun saveUserWithoutImage(uid: String, name: String, phone: String?) {
        val user = User(uid, name, phone, "No Image")
        saveUserToDatabase(user)
    }

    private fun saveUserToDatabase(user: User) {
        database?.reference?.child("users")?.child(user.uid!!)
            ?.setValue(user)
            ?.addOnCompleteListener {
                dialog?.dismiss()
                navigateToMainActivity()
            }
            ?.addOnFailureListener {
                showError("Failed to save user profile")
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        dialog?.dismiss()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectImg = uri
                binding.imgAvata.setImageURI(uri)
            }
        }
    }


}