package com.taruninc.kit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.taruninc.kit.database.User
import com.taruninc.kit.database.UserViewModel
import kotlinx.android.synthetic.main.fragment_add_new.view.*
import android.content.Context.INPUT_METHOD_SERVICE
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import com.google.firebase.database.FirebaseDatabase


class AddNewFragment : Fragment() {

    private lateinit var mUserViewModel: UserViewModel
    private lateinit var mAddUserViewModel: AddNewViewModel



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


        view.tv_temp.text = mAddUserViewModel.temperature.toString() // set if any existing value


        view.btn_get_temp.setOnClickListener {
            mAddUserViewModel.setRandom()
            view.tv_temp.text = mAddUserViewModel.temperature.toString()
        }


        view.floating_save.setOnClickListener {
            val firstName = view.et_firstname.text.toString()
            val lastName = view.et_lastname.text.toString()
            val temperature = view.tv_temp.text.toString().toFloat()

            if(inputCheck(firstName, lastName, temperature)) {
                val user = User(0, firstName, lastName, temperature)
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

    fun inputCheck(firstname: String, lastname: String, temp: Float): Boolean {
        return !(TextUtils.isEmpty(firstname) && TextUtils.isEmpty(lastname) && (temp > 0))
    }

    fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun onClickForAddNew() {
        val rootNode = FirebaseDatabase.getInstance()
    }
}
