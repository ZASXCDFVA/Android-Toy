package com.github.azsxcdfva.toy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.room.Room
import com.github.azsxcdfva.toy.data.Database
import com.github.azsxcdfva.toy.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {
    private val self = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(this, Database::class.java, "logins")
            .fallbackToDestructiveMigration()
            .build()

        val binding = ActivityLoginBinding
            .inflate(layoutInflater, findViewById(android.R.id.content), false)
            .apply {
                register.setOnClickListener {
                    startActivity(Intent(self, RegisterActivity::class.java))
                }

                login.setOnClickListener {
                    val username = username.text?.toString() ?: ""
                    val password = password.text?.toString() ?: ""

                    when {
                        username == "" -> {
                            Snackbar.make(root, R.string.empty_username, Snackbar.LENGTH_LONG)
                                .show()
                        }
                        password == "" -> {
                            Snackbar.make(root, R.string.empty_password, Snackbar.LENGTH_LONG)
                                .show()
                        }
                        else -> {
                            launch {
                                if (database.users().verify(username, password)) {
                                    setResult(Activity.RESULT_OK)

                                    finish()
                                } else {
                                    Snackbar.make(
                                        root,
                                        R.string.invalid_password,
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }

        setContentView(binding.root)
    }
}