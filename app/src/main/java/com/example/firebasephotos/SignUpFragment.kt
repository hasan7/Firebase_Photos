package com.example.firebasephotos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firebasephotos.databinding.SignupBinding
import com.example.firebasephotos.util.Resource
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {

    private lateinit var binding: SignupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: Viewmodel by viewModels()
        val name = binding.yourName
        val email = binding.email
        val password = binding.password
        val signup = binding.signup
        val error = binding.error
        val goSignin = binding.goSignin

        signup.setOnClickListener {
            lifecycleScope.launch {
                if (name.text.isNotEmpty() && email.text.isNotEmpty() && password.text.isNotEmpty()){
                    viewModel.signUp(name.text.toString(), email.text.toString(), password.text.toString())
                }
            }
        }

        goSignin.setOnClickListener {
            findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToSignInFragment())
        }

        lifecycleScope.launch {
            viewModel.loggedIn.collect {

                when(it){
                    is Resource.Success -> {Toast.makeText(this@SignUpFragment.context, "verification email has been sent", Toast.LENGTH_LONG).show()}
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