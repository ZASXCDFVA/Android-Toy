package com.github.azsxcdfva.toy

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.github.azsxcdfva.toy.data.Database
import com.github.azsxcdfva.toy.data.User
import com.github.azsxcdfva.toy.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(this, Database::class.java, "logins")
            .fallbackToDestructiveMigration()
            .build()

        val binding = ActivityRegisterBinding
            .inflate(LayoutInflater.from(this), findViewById(android.R.id.content), false)
            .apply {
                val passwordBinding = { _: Editable? ->
                    passwordVerifyLayout.error = when {
                        password.text?.isEmpty() != false -> {
                            null
                        }
                        password.text?.toString() != passwordVerify.text?.toString() -> {
                            getText(R.string.password_not_matched)
                        }
                        else -> {
                            null
                        }
                    }
                }

                password.addTextChangedListener(afterTextChanged = passwordBinding)
                passwordVerify.addTextChangedListener(afterTextChanged = passwordBinding)
                create.setOnClickListener {
                    val username = username.text?.toString() ?: ""
                    val email = email.text?.toString() ?: ""
                    val password = password.text?.toString() ?: ""
                    val passwordVerify = passwordVerify.text?.toString() ?: ""

                    when {
                        username == "" -> {
                            Snackbar.make(root, R.string.invalid_username, Snackbar.LENGTH_LONG)
                                .show()
                        }
                        password == "" -> {
                            Snackbar.make(root, R.string.invalid_password, Snackbar.LENGTH_LONG)
                                .show()
                        }
                        password != passwordVerify -> {
                            Snackbar.make(root, R.string.invalid_password, Snackbar.LENGTH_LONG)
                                .show()
                        }
                        else -> {
                            launch {
                                try {
                                    database.users().register(User(username, password, email))

                                    finish()
                                } catch (e: Exception) {
                                    Snackbar.make(root, e.toString(), Snackbar.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }

        setContentView(binding.root)
    }
}