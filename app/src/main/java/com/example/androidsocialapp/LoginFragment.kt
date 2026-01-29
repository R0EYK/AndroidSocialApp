package com.example.androidsocialapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidsocialapp.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    // ...existing code...
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // If user is already logged in, skip login screen
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_protectedArea)
            return
        }
        binding.btnRegisterTab.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.progressLogin.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    binding.progressLogin.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    if (task.isSuccessful) {
                        // Extract username from email (before @)
                        val username = email.substringBefore("@")
                        // You can use 'username' as needed here
                        findNavController().navigate(R.id.action_loginFragment_to_protectedArea)
                    } else {
                        Toast.makeText(requireContext(), "Login failed: " + (task.exception?.localizedMessage ?: "Unknown error"), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
