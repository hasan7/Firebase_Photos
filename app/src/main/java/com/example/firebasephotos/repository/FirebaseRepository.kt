package com.example.firebasephotos.repository


import android.net.Uri
import com.example.firebasephotos.util.Resource
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseRepository(
    private val firebaseAuth: FirebaseAuth,
    val storageRef: StorageReference
) {

     val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

     suspend fun login(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message!!)
        }
    }

     suspend fun signup(name: String, email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result?.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())?.await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(message = e.message.toString())
        }
    }

    suspend fun UploadPhoto(uri: Uri): UploadTask = withContext(Dispatchers.IO){

        return@withContext storageRef.child("images/${currentUser?.uid}${"/"}${System.currentTimeMillis()}").putFile(uri)
    }

    suspend fun DeletePhoto(name: String): Task<Void> = withContext(Dispatchers.IO){


        return@withContext storageRef.child("images/${currentUser?.uid}${"/"}${name}").delete()
    }

    fun isEmailVerified(): Boolean? {
       return currentUser?.isEmailVerified
    }


    suspend fun photoname(uri: Uri) : String = withContext(Dispatchers.IO){

        return@withContext FirebaseStorage.getInstance().getReferenceFromUrl(uri.toString()).name
    }

    suspend fun UploadProfilePhoto(uri: Uri): Task<Void> = withContext(Dispatchers.IO) {

        val profileUpdates = userProfileChangeRequest {
            photoUri = uri
        }
        return@withContext currentUser!!.updateProfile(profileUpdates)
    }

    suspend fun sendVerifyEmail(): Task<Void> = withContext(Dispatchers.IO) {

        return@withContext currentUser!!.sendEmailVerification()
    }

    suspend fun getUrls(): MutableList<Uri> = withContext(Dispatchers.IO){
        val uris = mutableListOf<Uri>()
        val listRef = storageRef.child("images/${currentUser?.uid}${"/"}")
        listRef.listAll().await().items.forEach {
            val uri = it.downloadUrl.await()
            uris.add(uri)
        }
        return@withContext uris
    }

     fun signOut() {
        firebaseAuth.signOut()
    }
}