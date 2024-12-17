package com.example.do_an

import android.app.ProgressDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.do_an.adapter.UserAdapter
import com.example.do_an.databinding.ActivityMainBinding
import com.example.do_an.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var database: FirebaseDatabase? = null
    var users: ArrayList<User>? = null
    var userAdapter: UserAdapter? = null
    var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this@MainActivity)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)

        database = FirebaseDatabase.getInstance()
        users = ArrayList<User>()
        userAdapter = UserAdapter(this@MainActivity, users!!)
        val layoutmanager = GridLayoutManager(this@MainActivity, 2)
        binding.rwUser.layoutManager = layoutmanager
        database!!.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users!!.clear()
                for (snapshot1 in snapshot.children) {
                    val user: User? = snapshot1.getValue(User::class.java)
                    if (!user!!.uid.equals(FirebaseAuth.getInstance().uid)) users!!.add(user)
                }
                userAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }
        )
    }

    override fun onResume() {
        super.onResume()
        val currentID = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentID!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentID = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentID!!).setValue("Offline")
    }
}