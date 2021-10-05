package com.sophia.chatsapp.activityes

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.sophia.chatsapp.adapter.UsersAdapter
import com.sophia.chatsapp.databinding.ActivityUsersBinding
import com.sophia.chatsapp.listeners.UserListener
import com.sophia.chatsapp.model.User
import com.sophia.chatsapp.utillties.Constants
import com.sophia.chatsapp.utillties.PreferenceManager

class UsersActivity : AppCompatActivity(), UserListener {

    private lateinit var binding: ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var userAdapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)

        setListener()
        getUsers()
    }

    private fun setListener() {
        binding.imageBack.setOnClickListener { onBackPressed() }
    }

    private fun showErrorMessage() {
        binding.textErrorMessage.setText(String.format("No user available"))
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun getUsers() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.kEY_COLLECTION_UESRS)
            .get()
            .addOnCompleteListener { task ->
                loading(false)
                val currentUserId = preferenceManager.getString(Constants.KEY_UESR_ID)
                if (task.isSuccessful && task.result != null) {
                    val users: ArrayList<User> = ArrayList()
                    for (queryDocumentSnapshot: QueryDocumentSnapshot in task.result!!) {
                        if (currentUserId.equals(queryDocumentSnapshot.id)) {
                            continue
                        }

                        val user = User()

                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME)!!
                        user.email = queryDocumentSnapshot.getString(Constants.KET_EMAIL)!!
                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE)!!
                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN).toString()
                        user.id = queryDocumentSnapshot.id

                        users.add(user)
                    }
                    userAdapter = UsersAdapter(users, this)
                    binding.userRecyclerview.adapter = userAdapter
                    binding.userRecyclerview.visibility = View.VISIBLE
                    binding.userRecyclerview.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
                    userAdapter.submitList(users)

                } else {
                    showErrorMessage()
                }

            }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER,user)
        startActivity(intent)
        finish()
        Log.d(TAG, "USER--${user}")
    }
}