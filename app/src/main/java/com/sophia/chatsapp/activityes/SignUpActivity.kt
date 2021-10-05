package com.sophia.chatsapp.activityes

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.FirebaseFirestore
import com.sophia.chatsapp.databinding.ActivitySignUpBinding
import com.sophia.chatsapp.utillties.Constants
import com.sophia.chatsapp.utillties.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import kotlin.collections.HashMap

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var encodedImage: String
    private lateinit var preFerenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preFerenceManager = PreferenceManager(applicationContext)
        setListeners()
    }

    private fun setListeners() {
        binding.textSignIn.setOnClickListener {v -> onBackPressed()}
        binding.buttonSignUp.setOnClickListener {
            signUp()
        }
        binding.layoutImage.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            fileterActivityLauncher.launch(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }

    private fun encodeImages(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight,false)
        val byteArrayOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes,Base64.DEFAULT)
    }

        private  val fileterActivityLauncher: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    if (it.data != null) {
                        val imageUri = it.data!!.data
                        try {
                            val inputStream = contentResolver.openInputStream(imageUri!!)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            binding.imageProfile.setImageBitmap(bitmap)
                            binding.textAddImage.visibility = View.GONE
                            encodedImage = encodeImages(bitmap)
                        }catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }
                    }
                }
            }

    private fun signUp() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val user: HashMap<String,Any> = HashMap()
        user.put(Constants.KEY_NAME,binding.inputName.text.toString())
        user.put(Constants.KET_EMAIL, binding.inputEmail.text.toString())
        user.put(Constants.KET_PASSWORD,binding.inputPassword.text.toString())
        user.put(Constants.KEY_IMAGE,encodedImage)
        database.collection(Constants.kEY_COLLECTION_UESRS)
            .add(user)
            .addOnSuccessListener { documentReference ->
                loading(false)
                preFerenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
                preFerenceManager.putString(Constants.KEY_UESR_ID,documentReference.id)
                preFerenceManager.putString(Constants.KEY_NAME,binding.inputName.text.toString())
                preFerenceManager.putString(Constants.KEY_IMAGE,encodedImage)
                val intent = Intent(applicationContext,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener { exception ->
                loading(false)
                showToast(exception.message!!)
            }
    }

//    private fun isValidSignUpDetails(): Boolean {
//        if (encodedImage == null) {
//            showToast("Select profile image")
//            return false
//        } else if (binding.inputName.text.toString().trim().isEmpty()) {
//            showToast("Enter name")
//            return false
//        } else if (binding.inputEmail.text.toString().trim().isEmpty()) {
//            showToast("Enter email")
//            return false
//        } else if (Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
//            showToast("Enter valid image")
//            return false
//        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
//            showToast("Enter password")
//            return false
//        } else if (binding.inputConfirmPassword.text.toString().trim().isEmpty()) {
//            showToast("Confirm your password")
//        } else if (binding.inputPassword.text.toString().equals(binding.inputConfirmPassword.text.toString())) {
//            showToast("Password & confirm password must be same")
//            return false
//        }
//        return true
//    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignUp.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonSignUp.visibility = View.VISIBLE
        }
    }
}