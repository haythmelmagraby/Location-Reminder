package com.udacity.project4.authentication

import android.app.Application
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthenticationViewModel(application: Application) : AndroidViewModel(application) {
    private val _firebaseUser = MutableLiveData<FirebaseUser>()
    val firebaseUser: MutableLiveData<FirebaseUser>
        get() = _firebaseUser

    init {
        _firebaseUser.value = FirebaseAuth.getInstance().currentUser
    }

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (AppCompatActivity.RESULT_OK == result.resultCode) {
            _firebaseUser.value = FirebaseAuth.getInstance().currentUser
        } else {
            Toast.makeText(getApplication(), result.idpResponse?.error?.message, Toast.LENGTH_SHORT)
                .show()
        }
    }
}