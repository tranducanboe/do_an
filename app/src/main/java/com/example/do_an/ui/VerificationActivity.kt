package com.example.do_an.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.do_an.databinding.ActivityVerificationBinding
import com.google.firebase.auth.FirebaseAuth

class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding
    var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (auth.currentUser != null) {
            startActivity(Intent(this@VerificationActivity, MainActivity::class.java))
            finish()
        }

        supportActionBar?.hide()
        binding.edtNumber.requestFocus()

        binding.btnContinue.setOnClickListener {
            val phoneNumber = binding.edtNumber.text.toString().trim()

            if (phoneNumber.isNotEmpty()) {
                // Thêm mã quốc gia vào số điện thoại nếu cần
                val formattedPhoneNumber = if (!phoneNumber.startsWith("+")) {
                    "+84$phoneNumber"  // Mã quốc gia Việt Nam
                } else {
                    phoneNumber
                }

                val intent = Intent(this@VerificationActivity, OtpActivity::class.java)
                intent.putExtra("phoneNumber", formattedPhoneNumber)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
