package com.miti.northrover


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.miti.northrover.Model.UserModel
import com.miti.northrover.databinding.ActivityGsigninscreenBinding


class GSignInScreen : AppCompatActivity() {

    lateinit var binding: ActivityGsigninscreenBinding
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 123
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var dbRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGsigninscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dbRef = db!!.getReference("Users")

        createRequest()

        binding.cardGoogleSignIn.setOnClickListener {
            Toast.makeText(this@GSignInScreen, "Clicked", Toast.LENGTH_SHORT)
                .show()
            signIn()
        }

    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onStart() {
        super.onStart()

        Toast.makeText(this, "N O R T H   R O V E R S", Toast.LENGTH_SHORT).show()

        val user = mAuth!!.currentUser
        if (user != null) {
            val intent = Intent(this@GSignInScreen, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    val user = mAuth!!.currentUser
                    val model = UserModel(
                        user!!.displayName.toString(),
                        user.email.toString(),
                        "empty",
                        "epmty"
                    )

                    val uid = mAuth!!.uid
                    dbRef!!.child(uid!!).setValue(model)

                    val intent = Intent(this@GSignInScreen, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@GSignInScreen, "Sorry auth failed!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}