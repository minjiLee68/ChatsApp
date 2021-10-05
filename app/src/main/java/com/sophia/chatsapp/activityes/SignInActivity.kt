package com.sophia.chatsapp.activityes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.sophia.chatsapp.databinding.ActivitySignInBinding
import com.sophia.chatsapp.utillties.Constants
import com.sophia.chatsapp.utillties.PreferenceManager

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var preferences: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager(applicationContext)

        if (preferences.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
    }

    private fun setListeners() {
        binding.textCreateNewAccount.setOnClickListener { v ->
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
        }
        binding.buttonSignIn.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.kEY_COLLECTION_UESRS)
            .whereEqualTo(Constants.KET_EMAIL, binding.etEmail.text.toString())
            .whereEqualTo(Constants.KET_PASSWORD, binding.etPassword.text.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0) {
                    val documentSnapshot: DocumentSnapshot = task.result!!.documents.get(0)
                    preferences.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferences.putString(Constants.KEY_UESR_ID, documentSnapshot.id)
                    preferences.putString(
                        Constants.KEY_NAME,
                        documentSnapshot.getString(Constants.KEY_NAME)!!
                    )
                    preferences.putString(
                        Constants.KEY_IMAGE,
                        documentSnapshot.getString(Constants.KEY_IMAGE)!!
                    )
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity((intent))
                } else {
                    loading((false))
                    showToast("Unable to sign in")
                }
            }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignIn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.buttonSignIn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

//    private fun isValidSignUpDetails(): Boolean {
//
//        if (binding.etEmail.text.toString().trim().isEmpty()) {
//            showToast("Enter email")
//            return false
//        } else if (binding.etPassword.text.toString().trim().isEmpty()) {
//            showToast("Enter password")
//            return false
//        }
//        return true
//    }

}