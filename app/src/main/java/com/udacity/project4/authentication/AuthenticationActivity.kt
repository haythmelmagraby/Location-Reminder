package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    // read the documentation
    //https://firebase.google.com/docs/auth/android/firebaseui
    private lateinit var authenticationViewModel: AuthenticationViewModel
    private lateinit var binding: ActivityAuthenticationBinding
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        authenticationViewModel.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        binding = DataBindingUtil.setContentView(this,R.layout.activity_authentication)


        authenticationViewModel.firebaseUser.observe(this) {
            if (it != null) {
                goToReminderActivity()
            }
        }
        binding.registerButton.setOnClickListener{
            Log.i("AuthenticationActivity","button clicked")
            createAuthentication()
        }

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    private fun goToReminderActivity() {
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()
    }

     fun createAuthentication(){
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }
}
