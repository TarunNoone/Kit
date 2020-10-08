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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.taruninc.kit.database.User
import com.taruninc.kit.database.UserViewModel
import kotlinx.android.synthetic.main.fragment_add_new.view.*
import android.widget.TextView


class AddNewFragment : Fragment() {

    private lateinit var mUserViewModel: UserViewModel
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
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
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

            if(inputCheck(firstName, lastName, temperature, qrInfo)) {
                val user = User(0, firstName, lastName, temperature, qrInfo)
                mUserViewModel.addNewUser(user)

                Toast.makeText(requireContext(), "Data Added Successfully", Toast.LENGTH_SHORT).show()

                hideKeyboard(view)
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    fun inputCheck(firstname: String, lastname: String, temp: Float, qrInfo: String): Boolean {
        return (firstname != "" && lastname != "" && temp > 0 && qrInfo != "")
//        return !(TextUtils.isEmpty(firstname) && TextUtils.isEmpty(lastname) && (temp > 0))
    }

    fun hideKeyboard(view: View) {
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
                qrInfo1.text = "QR Info: " + contents
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(requireContext(), "QR Scan failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
