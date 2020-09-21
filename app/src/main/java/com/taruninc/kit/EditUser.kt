package com.taruninc.kit

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
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
import kotlinx.android.synthetic.main.fragment_edit_user.view.*


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

        view.et_firstname.setText(args.currentUser.userFirstName)
        view.et_lastname.setText(args.currentUser.userLastName)

        mAddUserViewModel.temperature = args.currentUser.userTemperature
        view.tv_temp.text = mAddUserViewModel.temperature.toString()

        view.btn_get_temp.setOnClickListener {
            // implement correct method to get temperature here
            mAddUserViewModel.setRandom()
            view.tv_temp.text = mAddUserViewModel.temperature.toString()
        }

        view.floating_edit.setOnClickListener {
            val firstname = view.et_firstname.text.toString()
            val lastname = view.et_lastname.text.toString()
            val temperature = mAddUserViewModel.temperature

            // Validate input and update user
            if(inputCheck(firstname, lastname, temperature)) {
                val updatedUser = User(args.currentUser.UUID, firstname, lastname, temperature)
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

    fun inputCheck(firstname: String, lastname: String, temp: Float): Boolean {
        return !(TextUtils.isEmpty(firstname) && TextUtils.isEmpty(lastname) && (temp > 0))
    }
}
