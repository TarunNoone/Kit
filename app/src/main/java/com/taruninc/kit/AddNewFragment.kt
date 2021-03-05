package com.taruninc.kit

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_add_new.view.*


class AddNewFragment : Fragment() {
    private lateinit var mAddUserViewModel: AddNewViewModel
    private lateinit var qrInfo1: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_new, container, false)

        // Get the ViewModels for this activity
        mAddUserViewModel = ViewModelProvider(this).get(AddNewViewModel::class.java)

        qrInfo1 = view.tv_qr_info

        view.tv_temp.text = mAddUserViewModel.temperature.toString() // set if any existing value
        view.tv_qr_info.text = "QR Info: " + mAddUserViewModel.qrInfo // set if any existing value


        view.btn_get_temp.setOnClickListener {
            mAddUserViewModel.setRandom()
            view.tv_temp.text = mAddUserViewModel.temperature.toString()
        }

        view.btn_scan_qr.setOnClickListener {
            try {
                val intent = Intent("com.google.zxing.client.android.SCAN")
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE") // "PRODUCT_MODE" for bar codes
                startActivityForResult(intent, 0)
            } catch (e: Exception) {
                val marketUri: Uri =
                    Uri.parse("market://details?id=com.google.zxing.client.android")
                val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                startActivity(marketIntent)
            }
        }


        view.floating_save.setOnClickListener {
            val firstName = view.et_firstname.text.toString()
            val lastName = view.et_lastname.text.toString()
            val temperature = mAddUserViewModel.temperature
            val qrInfo = mAddUserViewModel.qrInfo
            val aadharId = view.et_aadhar.text.toString()
            val age = view.et_age.text.toString().toInt()
            val phoneNum = view.et_phoneNum.text.toString()
            val gender: Boolean = view.rb_M.isSelected

            if(inputCheck(aadharId, firstName, lastName, age, phoneNum, temperature, qrInfo)) {
                saveInfoToDb(aadharId, firstName, lastName,
                    age, gender, phoneNum, temperature, qrInfo,
                    view.et_medHist.text.toString())
                hideKeyboard(view)
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun inputCheck(
        aadharId: String, firstName: String, lastName: String,
        age: Int, phoneNum: String,
        temperature: Float, qrInfo: String
    ): Boolean {
        return (aadharId != "" && firstName != "" && lastName != "" &&
                age > 0 && phoneNum != "" &&
                temperature > 0 && qrInfo != "")
    }

    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                val contents = data?.getStringExtra("SCAN_RESULT")
                if (contents != null) {
                    mAddUserViewModel.qrInfo = contents
                }
                qrInfo1.text = "QR Info: $contents"
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(requireContext(), "QR Scan failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveInfoToDb(
        aadharId: String, firstName: String, lastName: String,
        age: Int, gender: Boolean, phoneNum: String,
        temperature: Float, qrInfo: String, medHist: String
    ) {
        // Write a message to the database
        val user = Firebase.auth.currentUser

        user!!.getIdToken(false).addOnSuccessListener { result ->
            val isVaccinator = (result.claims["isVaccinator"] == true)

            if (isVaccinator) {
                val database = Firebase.database
//                val unixTime = (System.currentTimeMillis() / 1000).toString()
                val newUser = User(
                    firstName, lastName,
                    gender, age, phoneNum, temperature, qrInfo,
                    medHist
                )

                val ref = database.getReference("patients/$aadharId")
                ref.setValue(newUser)

                Toast.makeText(requireContext(), "New user added successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}