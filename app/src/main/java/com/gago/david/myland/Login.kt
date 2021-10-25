package com.gago.david.myland

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import com.gago.david.myland.adapters.UserAdapter
import android.view.inputmethod.EditorInfo


class Login : AppCompatActivity() {

    var users: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //deleteDatabase("myland.db")
        users = LandOpenHelper.readUsers(this)
        setContentView(R.layout.activity_login)
        val recyclerView: RecyclerView? = findViewById(R.id.users)
        recyclerView!!.layoutManager =
            LinearLayoutManager(this)
        recyclerView.adapter = UserAdapter(users, this){
            selectUser(it)
        }
    }

    private fun selectUser(user: String){
        val editor = getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE).edit()
        editor.putString("user", user)
        editor.apply()
        val intent = MainActivity.newIntent(this, user)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    fun createUser (view: View) {
        val alertDialog = AlertDialog.Builder(view.rootView.context)
        alertDialog.setTitle(R.string.create_profile)
        alertDialog.setMessage(R.string.insert_name)
        val input = EditText(this)
        var dialog: AlertDialog? = null
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        input.setSingleLine()
        input.gravity = Gravity.CENTER
        input.left = 8
        input.right = 8
        input.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
        alertDialog.setView(input)

        fun createUser2(name: String){
            //Toast.makeText(this, "$name Clicked", Toast.LENGTH_SHORT).show()
            LandOpenHelper.createUser(this, name)
            if (users!!.size==0)
                LandOpenHelper.updateLands(this)
            users!!.add(name)
            selectUser(name)
        }
        input.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //do stuff here
                createUser2(v.text.toString())
                dialog!!.dismiss()
                false
            } else false
        }

        alertDialog.setPositiveButton("OK"
        ) { _, _ ->
            createUser2(input.text.toString())
        }

        alertDialog.setNegativeButton(R.string.cancel
        ) { dialog2, _ -> dialog2.cancel() }
        dialog = alertDialog.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.show()
    }
}
