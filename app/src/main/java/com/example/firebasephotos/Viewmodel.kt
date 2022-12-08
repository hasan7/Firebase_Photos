package com.example.firebasephotos

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebasephotos.repository.FirebaseRepository
import com.example.firebasephotos.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class Viewmodel : ViewModel() {

    val firebaseAuth = FirebaseAuth.getInstance()
    val storageReference = FirebaseStorage.getInstance().reference
    val repository = FirebaseRepository(firebaseAuth, storageReference)
    val currentUser = repository.currentUser

    private var _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.asStateFlow()

    private var _loggedIn = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val loggedIn = _loggedIn.asStateFlow()

    //verified value to be used for signing In, also to handle sign out since it returns false
    private var _isVerfied = MutableStateFlow<Boolean?>(null)
    val isVerfied = _isVerfied.asStateFlow()

    private var _urls = MutableStateFlow<List<Uri>?>(null)
    val urls = _urls.asStateFlow()



     fun getUrls() {
            _isLoading.value = true
            viewModelScope.launch {
                _urls.value = repository.getUrls()
                _isLoading.value = false
            }
    }
    fun uploadPhoto(uri: Uri){

        viewModelScope.launch {
            val task = repository.UploadPhoto(uri)
            task.addOnSuccessListener {
                Log.d("UploadImage", "Task Is Successful")

                getUrls()
            }
                .addOnFailureListener {
                    Log.d("UploadImageFail", "Image Upload Failed ${it.printStackTrace()}")
                }
        }
    }

     fun isEmailVerified(): Boolean? {
        return repository.isEmailVerified()
    }

    fun sendVerifyEmail() {
        viewModelScope.launch {
            repository.sendVerifyEmail().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                }
            }
        }

    }

    fun deletePhoto(uri: Uri){

        viewModelScope.launch {
            val task = repository.DeletePhoto(repository.photoname(uri))
            task.addOnSuccessListener {
                Log.d("DeletePhoto", "Task Is Successful")

                getUrls()
            }
                .addOnFailureListener {
                    Log.d("DeletePhotoFail", "Delete Photo Failed ${it.printStackTrace()}")
                }
        }
    }


    suspend fun photoName(uri: Uri) : String{
        val x =viewModelScope.async {
             repository.photoname(uri)
        }.await()
        return x
    }


    fun uploadProfilePhoto(uri: Uri){
        viewModelScope.launch {
            val task = repository.UploadProfilePhoto(uri)
            task.addOnCompleteListener {
                Log.d("UploadProfileImage", "Task Is Successful")
            }
                .addOnFailureListener {
                    Log.d("UploadProfileImage", "Image Upload Failed ${it.printStackTrace()}")
                }
        }
    }
    fun signIn(email:String, password:String){
        viewModelScope.launch {
            _isVerfied.value = null
            val result = repository.login(email, password)
            _isVerfied.value = isEmailVerified()
            _loggedIn.value = result
        }

    }

    fun signOut() {
        repository.signOut()
    }
    fun signUp(name: String, email:String, password:String){
        viewModelScope.launch {
            val result = repository.signup(name, email, password)
            when(result){
                is Resource.Success -> sendVerifyEmail()
                else -> Resource.Error("Error",result)
            }
            _loggedIn.value = result
        }

    }




}