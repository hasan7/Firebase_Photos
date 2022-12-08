package com.example.firebasephotos

import android.R
import android.app.Dialog
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.firebasephotos.databinding.PhotoFullscreenBinding
import com.example.firebasephotos.databinding.PhotosBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch


class PhotosFragment : Fragment() , onGridListener {


    lateinit var dialogImageView: ImageView
    lateinit var dialogbtn: FloatingActionButton
    lateinit var dialog: Dialog
    private lateinit var binding: PhotosBinding
    private lateinit var PhotoFullscreen: PhotoFullscreenBinding
    val viewModel: Viewmodel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val userInfo = binding.infos
        val userInfoEmail = binding.infosEmail
        val button = binding.btn1
        val profilePhoto = binding.profileImg
        val progressBar = binding.progressBar
        val adapter = GallaryAdapter(this@PhotosFragment)
        val gridLayoutManager = GridLayoutManager(binding.root.context,3)

        PhotoFullscreen = PhotoFullscreenBinding.inflate(layoutInflater)
        dialogImageView = PhotoFullscreen.imageFullscreen

        dialogbtn = PhotoFullscreen.btnDelete

        dialog = Dialog(binding.root.context, R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(PhotoFullscreen.root)
        dialog.setCancelable(true)

        binding.recyclerviewGrid.adapter = adapter
        binding.recyclerviewGrid.layoutManager = gridLayoutManager

        viewModel.getUrls()

        lifecycleScope.launch {

            viewModel.urls.collect {
                if (it != null){
                    adapter.data = it!!
                }
            }
        }

        lifecycleScope.launch {

            viewModel.isLoading.collect {
                if(it){
                    progressBar.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.INVISIBLE
                }
            }
        }

        val imageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
            uri?.let {
                viewModel.uploadPhoto(it)
            }
        }

        userInfo.text = viewModel.currentUser?.displayName?.replaceFirstChar(Char::titlecase)
        userInfoEmail.text = viewModel.currentUser?.email

        button.setOnClickListener {
            //image/* here is the type we expect from the intent. (image/jpeg audio/mpeg4-generic text/html audio/mpeg)
            imageLauncher.launch("image/*")
        }

        // due to to android 11 restriction, I cant access internal "content" files where firebase store
        // the profile picture
//        profilePhoto.setOnClickListener {
//            profileLauncher.launch("image/*")
//        }

        //        val profileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
//            uri?.let {
//               viewModel.uploadProfilePhoto(uri)
//            }
//        }

    }

    override fun onClick(url: Uri) {
        Glide
            .with(binding.root.context)
            .load(url.toString())
            .into(dialogImageView)

        dialog.show()

        dialogbtn.setOnClickListener {
            viewModel.deletePhoto(url)
            dialog.hide()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: CALLED photo")
        dialog.dismiss()
        viewModel.signOut()
    }

}