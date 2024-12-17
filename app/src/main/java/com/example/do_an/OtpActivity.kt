package com.example.do_an

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.do_an.databinding.ActivityOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private var verificationId: String? = null
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()

        // Nhận số điện thoại từ Intent
        val phone = intent.getStringExtra("phoneNumber")
        if (phone.isNullOrBlank()) {
            Toast.makeText(this, "Invalid phone number!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.txtPhone.text = "Verify $phone"

        // Hiển thị ProgressBar khi gửi OTP
        binding.progressBar.visibility = android.view.View.VISIBLE

        val options = PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Xác minh tự động thành công (nếu có)
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Ẩn ProgressBar và thông báo lỗi
                    binding.progressBar.visibility = android.view.View.GONE
                    val errorMessage = when (e) {
                        is FirebaseAuthInvalidCredentialsException -> "Invalid phone number format."
                        else -> e.message ?: "Verification failed."
                    }
                    Toast.makeText(this@OtpActivity, errorMessage, Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verifyId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    // Lưu verificationId để sử dụng cho việc xác minh OTP
                    verificationId = verifyId
                    binding.progressBar.visibility = android.view.View.GONE

                    // Hiển thị bàn phím cho người dùng nhập mã OTP
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    binding.otpView.requestFocus()
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        // Lắng nghe khi người dùng nhập mã OTP
        binding.otpView.setOtpCompletionListener { otp ->
            if (verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Verification ID is null. Retry the process.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Thành công: chuyển sang màn hình SetupProfileActivity
                    startActivity(Intent(this@OtpActivity, SetupProfileActivity::class.java))
                    finishAffinity()
                } else {
                    // Lỗi đăng nhập
                    Toast.makeText(this@OtpActivity, "Sign-in failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
