package com.taruninc.kit.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application): AndroidViewModel(application) {

    val readAllUsers: LiveData<List<User>>

    private val userDao: UserDao = UserDatabase.getDatabase(application).userDao()

    init {
        readAllUsers = userDao.getAllUsers()
    }

    fun addNewUser(newUser: User) {
        // Database jobs on main thread might hang the app
        // Run the jobs on background thread
        viewModelScope.launch(Dispatchers.IO) {
            userDao.insertNewUser(newUser)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.updateUser(user)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.deleteUser(user)
        }
    }

}