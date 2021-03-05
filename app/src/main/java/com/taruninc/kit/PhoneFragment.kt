package com.taruninc.kit

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_phone.*
import kotlinx.android.synthetic.main.fragment_phone.view.*
import java.util.concurrent.TimeUnit


class PhoneFragment : Fragment() {

    private val args: PhoneFragmentArgs by navArgs()
    private lateinit var userType: String

    private lateinit var auth: FirebaseAuth
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var storedVerificationId: String

    private var otpSent: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userType = args.userType
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_phone, container, false)

        view.btn_getOTP.setOnClickListener {
            hideKeyboard(view)
            if(!otpSent) {
                val phoneNumber = "+91" + view.et_phone.text.toString()
                progressBarPhone.visibility = View.VISIBLE
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber, // Phone number to verify
                    60, // Timeout duration
                    TimeUnit.SECONDS, // Unit of timeout
                    requireActivity(), // Activity (for callback binding)
                    callbacks
                ) // OnVerificationStateChangedCallbacks
                progressBarPhone.visibility = View.VISIBLE
            } else {
                progressBarPhone.visibility = View.GONE
                val credential = PhoneAuthProvider.getCredential(
                    storedVerificationId,
                    view.et_otp.text.toString()
                )
                signInWithPhoneAuthCredential(credential)
            }
        }

        return view
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(ContentValues.TAG, "onVerificationCompleted:$credential")
            progressBarPhone.visibility = View.GONE
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(ContentValues.TAG, "onVerificationFailed", e)

            progressBarPhone.visibility = View.GONE

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }

            // Show a message and update the UI
            // ...
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(ContentValues.TAG, "onCodeSent:$verificationId")

            progressBarPhone.visibility = View.GONE

            btn_getOTP.text = "Verify OTP"
            et_phone.isEnabled = false
            et_otp.isEnabled = true

            otpSent = true

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token

            // ...
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "signInWithCredential:success")
                    Toast.makeText(requireContext(), "Logged in using Phone", Toast.LENGTH_SHORT).show()

                    navigateToCorrectPage()
                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(requireContext(), "Failed Phone Login", Toast.LENGTH_SHORT).show()

                    findNavController().popBackStack(R.id.selectTypeFragment, false)

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // TODO: Implement invalid verification code case
                    }
                }
            }
    }

    private fun navigateToCorrectPage() {
        if(userType == "Patient") {
            findNavController().navigate(R.id.action_phoneFragment_to_patientFragment)
            return
        }

        auth.currentUser?.getIdToken(true)?.addOnSuccessListener(OnSuccessListener<GetTokenResult> { result ->
            val isAdmin: Boolean = (result.claims["isVaccinator"] == true)
            if (isAdmin) {
                findNavController().navigate(R.id.action_phoneFragment_to_vaccinatorFragment)
            } else {
                findNavController().popBackStack(R.id.selectTypeFragment, false)
                Toast.makeText(requireContext(), "Permission Denied. Please contact supervisor.", Toast.LENGTH_LONG).show()
                auth.signOut()
            }
        })
    }

    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}