package com.taruninc.kit

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_select_type.*
import kotlinx.android.synthetic.main.fragment_select_type.view.*
import org.json.JSONObject


class SelectTypeFragment : Fragment(), View.OnClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        functions = Firebase.functions
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_select_type, container, false)

        view.btn_type_patient.setOnClickListener(this)
        view.btn_type_vaccinator.setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_type_patient -> navigateToUser("Patient");
            R.id.btn_type_vaccinator -> navigateToUser("Vaccinator");
        }
    }

    private fun navigateToUser(user: String) {
        var msg = "Test msg"
        functions.getHttpsCallable("welcome")
            .call()
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                val msg2 = it.data as HashMap<*, *>
                val msg3 = msg2["number"].toString().toInt()
                Toast.makeText(requireContext(), msg3, Toast.LENGTH_SHORT).show()
            }


//        Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT).show()

        if(auth.currentUser != null) {
            progressBar.visibility = View.VISIBLE
            navigateToCorrectPage(user)
        } else {
            val action = SelectTypeFragmentDirections.actionSelectTypeFragmentToLoginFragment(user)
            findNavController().navigate(action)
        }
    }

    private fun navigateToCorrectPage(userType: String) {
        if(userType == "Patient") {
            findNavController().navigate(R.id.action_selectTypeFragment_to_patientFragment)
            return
        }

        auth.currentUser?.getIdToken(true)?.addOnSuccessListener(OnSuccessListener<GetTokenResult> { result ->
            val isAdmin: Boolean = (result.claims["isVaccinator"] == true)
            if (isAdmin) {
                findNavController().navigate(R.id.action_selectTypeFragment_to_vaccinatorFragment)
            } else {
                progressBar.visibility = View.GONE
                Snackbar.make(
                    requireView(),
                    "Permission Denied. Please contact supervisor.",
                    Snackbar.LENGTH_LONG
                ).show()
                auth.signOut()
            }
        })
    }

}