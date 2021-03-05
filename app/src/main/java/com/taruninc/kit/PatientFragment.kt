package com.taruninc.kit

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_patient.*
import kotlinx.android.synthetic.main.fragment_patient.view.*

class PatientFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.log_out_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout_option -> {

                val builder: AlertDialog.Builder? = activity?.let {
                    AlertDialog.Builder(it)
                }
                builder?.setMessage("You will have to re-login")?.setTitle("Confirm")

                builder?.apply {
                    setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialog, id ->
//                            mUserViewModel.deleteUser(args.currentUser)
                            Firebase.auth.signOut()
                            Toast.makeText(requireContext(), "Log out Successfully", Toast.LENGTH_SHORT).show()
                            requireActivity().finish()
//                            findNavController().navigate(R.id.action_patientFragment_to_selectTypeFragment)
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
        val view = inflater.inflate(R.layout.fragment_patient, container, false)

        view.btn_search.setOnClickListener {
            progressBarEdit.visibility = View.VISIBLE
            hideKeyboard(view)
            val aadharId = et_aadharId.text.toString()
            et_aadharId.setText("")

            val database = Firebase.database
//            val uid = Firebase.auth.currentUser?.uid
            val myRef = database.getReference("patients/$aadharId")

            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    if(dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(User::class.java)
                        val action = PatientFragmentDirections
                            .actionPatientFragmentToEditUserFragment(
                                aadharId,
                                user!!
                            )
                        findNavController().navigate(action)
                        Toast.makeText(requireContext(), "Found user", Toast.LENGTH_SHORT).show()
                    } else {
                        progressBarEdit.visibility = View.GONE
                        Toast.makeText(requireContext(), "ID does not exist", Toast.LENGTH_SHORT).show()
                    }
                    // ...
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
                    // ...
                }
            }

            myRef.addListenerForSingleValueEvent(postListener)
        }

        return view
    }


    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}