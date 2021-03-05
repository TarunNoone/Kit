package com.taruninc.kit

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_edit_user.*
import kotlinx.android.synthetic.main.fragment_edit_user.view.*
import kotlinx.android.synthetic.main.fragment_edit_user.view.btn_get_temp
import kotlinx.android.synthetic.main.fragment_edit_user.view.et_aadhar
import kotlinx.android.synthetic.main.fragment_edit_user.view.et_age
import kotlinx.android.synthetic.main.fragment_edit_user.view.et_firstname
import kotlinx.android.synthetic.main.fragment_edit_user.view.et_lastname
import kotlinx.android.synthetic.main.fragment_edit_user.view.et_medHist
import kotlinx.android.synthetic.main.fragment_edit_user.view.et_phoneNum
import kotlinx.android.synthetic.main.fragment_edit_user.view.rb_M
import kotlinx.android.synthetic.main.fragment_edit_user.view.tv_qr_info
import kotlinx.android.synthetic.main.fragment_edit_user.view.tv_temp

class EditUserFragment : Fragment() {
    private val args: EditUserFragmentArgs by navArgs()
    private lateinit var mAddUserViewModel: AddNewViewModel

    private lateinit var currentUser: User
    private lateinit var aadharId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        aadharId = args.refPath
        currentUser = args.userInfo
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.delete_option -> {

                val builder: AlertDialog.Builder? = activity?.let {
                    AlertDialog.Builder(it)
                }
                builder?.setMessage("This action cannot be undone")?.setTitle("Confirm")

                builder?.apply {
                    setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialog, id ->
                            progressBarDelete.visibility = View.VISIBLE
                            // delete the path from firebase
                            Firebase.database.getReference(
                                "patients/${aadharId}"
                            ).removeValue()
                            Toast.makeText(requireContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        })
                    setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialog, id ->
                            // Do nothing.
                        })
                }

                val dialog: AlertDialog? = builder?.create()
                dialog?.show()

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_user, container, false)

        mAddUserViewModel = ViewModelProvider(this).get(AddNewViewModel::class.java)

        view.et_aadhar.setText(aadharId)
        view.et_firstname.setText(currentUser.userFirstName)
        view.et_lastname.setText(currentUser.userLastName)

        if(currentUser.userGender) {
            view.rb_gender2.check(R.id.rb_M)
        } else {
            view.rb_gender2.check(R.id.rb_F)
        }

        view.et_age.setText(currentUser.userAge.toString())
        view.et_phoneNum.setText(currentUser.userPhoneNumber)
        view.et_medHist.setText(currentUser.userMedHist)

        mAddUserViewModel.qrInfo = currentUser.userQRInfo
        view.tv_qr_info.text = "QR Info: " + currentUser.userQRInfo

        mAddUserViewModel.temperature = currentUser.userTemperature
        view.tv_temp.text = mAddUserViewModel.temperature.toString()

        view.btn_get_temp.setOnClickListener {
            // implement correct method to get temperature here
            mAddUserViewModel.setRandom()
            view.tv_temp.text = mAddUserViewModel.temperature.toString()
        }

        view.floating_edit.setOnClickListener {
            val firstName = view.et_firstname.text.toString()
            val lastName = view.et_lastname.text.toString()
            val temperature = mAddUserViewModel.temperature
            val qrInfo = mAddUserViewModel.qrInfo
            val aadharId = view.et_aadhar.text.toString()
            val age = view.et_age.text.toString().toInt()
            val phoneNum = view.et_phoneNum.text.toString()
            val gender: Boolean = view.rb_M.isSelected

            // Validate input and update user
            if(inputCheck(aadharId, firstName, lastName, age, phoneNum, temperature, qrInfo)) {
                // Brute force update
                saveInfoToDb(aadharId, firstName, lastName,
                    age, gender, phoneNum, temperature, qrInfo,
                    view.et_medHist.text.toString())
                Toast.makeText(requireContext(), "Updated Successfully", Toast.LENGTH_SHORT).show()
                hideKeyboard(view)
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Enter correct values", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun inputCheck(aadharId: String, firstName: String, lastName: String,
                           age: Int, phoneNum:String,
                           temperature: Float, qrInfo: String): Boolean {
        return (aadharId != "" && firstName != "" && lastName != "" &&
                age > 0 && phoneNum != "" &&
                temperature > 0 && qrInfo != "")
//        return !(TextUtils.isEmpty(firstname) && TextUtils.isEmpty(lastname) && (temp > 0))
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

            if(isVaccinator) {
                val database = Firebase.database
//                val unixTime = (System.currentTimeMillis() / 1000).toString()
                val updatedUser = User(
                    firstName, lastName,
                    gender, age, phoneNum, temperature,
                    qrInfo, medHist
                )

                val ref = database.getReference(
                    "patients/$aadharId"
                )
                ref.setValue(updatedUser)

                Toast.makeText(requireContext(), "Details updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}