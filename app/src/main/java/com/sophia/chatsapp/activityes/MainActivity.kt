package com.sophia.chatsapp.activityes

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.sophia.chatsapp.databinding.ActivityMainBinding
import com.sophia.chatsapp.utillties.Constants
import com.sophia.chatsapp.utillties.PreferenceManager
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)

        loadUserDetails()
        getToken()
        setListeners()
    }

    private fun setListeners() {
        binding.imageSignOut.setOnClickListener { v -> signOut() }
        binding.fadNewChat.setOnClickListener { v ->
            startActivity(Intent(applicationContext,UsersActivity::class.java))
        }
    }

    private fun loadUserDetails() {
        binding.textName.text = preferenceManager.getString(Constants.KEY_NAME)
        val bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    }

    private fun updateToken(token: String) {
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentReference: DocumentReference = database.collection(Constants.kEY_COLLECTION_UESRS)
            .document(preferenceManager.getString(Constants.KEY_UESR_ID))
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
            .addOnFailureListener { e -> showToast("Unable to update token") }
    }

    private fun signOut() {
        showToast("Signing out...")
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentReference: DocumentReference =
            database.collection(Constants.kEY_COLLECTION_UESRS).document(
                preferenceManager.getString(Constants.KEY_UESR_ID)
            )
        val updates: HashMap<String, Any> = HashMap()
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete())
        documentReference.update(updates)
            .addOnSuccessListener {  unused ->
                preferenceManager.clear()
                startActivity(Intent(applicationContext, SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener { e -> showToast("Unable to sign out") }
    }
}