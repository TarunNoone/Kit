package com.taruninc.kit

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.taruninc.kit.database.User
import com.taruninc.kit.database.UserViewModel
import kotlinx.android.synthetic.main.fragment_add_new.*
import kotlinx.android.synthetic.main.fragment_edit_user.view.*
import kotlinx.android.synthetic.main.fragment_edit_user.view.btn_get_temp
import kotlinx.android.synthetic.main.fragment_edit_user.view.et_aadhar
import kotlinx.android.synthetic.main.fragment_edit_user.view.et_age
import kotlinx.android.synthetic.main.fragment_edit_user.view.et_firstname
import kotlinx.android.synthetic.main.fragment_edit_user.view.et_lastname
import kotlinx.android.synthetic.main.fragment_edit_user.view.et_phoneNum
import kotlinx.android.synthetic.main.fragment_edit_user.view.rb_M
import kotlinx.android.synthetic.main.fragment_edit_user.view.tv_qr_info
import kotlinx.android.synthetic.main.fragment_edit_user.view.tv_temp


class EditUser : Fragment() {

    private val args by navArgs<EditUserArgs>()

    private lateinit var mUserViewModel: UserViewModel
    private lateinit var mAddUserViewModel: AddNewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
                            mUserViewModel.deleteUser(args.currentUser)
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

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        mAddUserViewModel = ViewModelProvider(this).get(AddNewViewModel::class.java)

        view.et_aadhar.setText(args.currentUser.userAadhar)
        view.et_firstname.setText(args.currentUser.userFirstName)
        view.et_lastname.setText(args.currentUser.userLastName)

        if(args.currentUser.userGender) {
            view.rb_gender2.check(R.id.rb_M)
        } else {
            view.rb_gender2.check(R.id.rb_F)
        }

        view.et_age.setText(args.currentUser.userAge.toString())
        view.et_phoneNum.setText(args.currentUser.userPhoneNumber)

        mAddUserViewModel.qrInfo = args.currentUser.userQRInfo
        view.tv_qr_info.text = "QR Info: " + args.currentUser.userQRInfo

        mAddUserViewModel.temperature = args.currentUser.userTemperature
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
//                val updatedUser = User(args.currentUser.UUID, firstname, lastname, temperature, args.currentUser.userQRInfo)
                val updatedUser = User(args.currentUser.UUID, aadharId, firstName, lastName, gender, age, phoneNum, temperature, qrInfo)
                mUserViewModel.updateUser(updatedUser)
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

    fun inputCheck(aadharId: String, firstName: String, lastName: String,
                   age: Int, phoneNum:String,
                   temperature: Float, qrInfo: String): Boolean {
        return (aadharId != "" && firstName != "" && lastName != "" &&
                age > 0 && phoneNum != "" &&
                temperature > 0 && qrInfo != "")
//        return !(TextUtils.isEmpty(firstname) && TextUtils.isEmpty(lastname) && (temp > 0))
    }

}
