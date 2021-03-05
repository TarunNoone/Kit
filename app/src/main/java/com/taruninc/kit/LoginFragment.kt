package com.taruninc.kit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*


class LoginFragment : Fragment(), View.OnClickListener {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val RC_GOOGLE_SIGNIN: Int = 100

    private val args: LoginFragmentArgs by navArgs()
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth
        val currentUser = auth.currentUser

        userType = args.userType

        if(currentUser != null) {
            navigateToCorrectPage()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        view.btn_google_signin.setSize(SignInButton.SIZE_WIDE)
        view.btn_google_signin.setOnClickListener(this)
        view.btn_phone_signin.setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        progressBarLogin.visibility = View.VISIBLE
        when(v?.id) {
            R.id.btn_google_signin -> googleSignIn()
            R.id.btn_phone_signin -> phoneSignIn()
        }
    }

    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGNIN) //assume 100 is for signIn
    }

    private fun phoneSignIn() {
        val action = LoginFragmentDirections.actionLoginFragmentToPhoneFragment(userType)
        findNavController().navigate(action);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGNIN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
            mGoogleSignInClient.signOut() //sign out of google client. firebase id is used instead.
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.idToken?.let { firebaseAuthWithGoogle(it) }
//            Toast.makeText(requireContext(), "Logged In", Toast.LENGTH_SHORT).show()
//            findNavController().navigate(R.id.action_loginFragment_to_blankFragment)
        } catch (e: ApiException) {
            progressBarLogin.visibility = View.GONE
            Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    navigateToCorrectPage()
                } else {
                    progressBarLogin.visibility = View.GONE
                    Toast.makeText(requireContext(), "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToCorrectPage() {
        if(userType == "Patient") {
            findNavController().navigate(R.id.action_loginFragment_to_patientFragment)
            return
        }

        auth.currentUser?.getIdToken(true)?.addOnSuccessListener(OnSuccessListener<GetTokenResult> { result ->
            val isAdmin: Boolean = (result.claims["isVaccinator"] == true)
            if (isAdmin) {
                findNavController().navigate(R.id.action_loginFragment_to_vaccinatorFragment)
            } else {
                progressBarLogin.visibility = View.GONE
                Snackbar.make(requireView(), "Permission Denied. Please contact supervisor.", Snackbar.LENGTH_LONG).show()
                auth.signOut()
            }
        })
    }

}