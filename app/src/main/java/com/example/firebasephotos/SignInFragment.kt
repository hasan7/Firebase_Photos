package com.example.firebasephotos


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firebasephotos.databinding.LoginBinding
import com.example.firebasephotos.util.Resource
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {

    private lateinit var binding: LoginBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: Viewmodel by viewModels()
        val email = binding.email
        val password = binding.password
        val login = binding.login
        val error = binding.error
        val goSignup = binding.goSignup

        //sign user out from previous session since we didnt impl creds storing,
        //also the app will ignore sign in attempts if signout didnt get called(will be logged in from
        // prevoius session even after app restart).
        viewModel.signOut()

        login.setOnClickListener {
            lifecycleScope.launch {
                if (email.text.isNotEmpty() && password.text.isNotEmpty()){
                   viewModel.signIn(email.text.toString(), password.text.toString())
                }
            }
        }

        goSignup.setOnClickListener {
            findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment())
        }

        lifecycleScope.launch {
            viewModel.isVerfied.collect {
                if (it == true){
                    findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToPhotosFragment())
                }
                if(it == false){
                    error.visibility = View.VISIBLE
                    error.text = "Please Verify your email"
                }
            }
        }

        lifecycleScope.launch {
            viewModel.loggedIn.collect {

                when(it){
                    is Resource.Success -> {}
                    is Resource.Error -> {
                        error.visibility = View.VISIBLE
                        error.text = it.message
                    }

                    else -> {}
                }
            }
        }


    }
}