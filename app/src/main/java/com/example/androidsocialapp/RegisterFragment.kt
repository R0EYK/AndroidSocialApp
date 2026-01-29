package com.example.androidsocialapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidsocialapp.databinding.FragmentRegisterBinding
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfileImage.setImageURI(it)
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && cameraImageUri != null) {
            selectedImageUri = cameraImageUri
            binding.ivProfileImage.setImageURI(cameraImageUri)
        }
    }

    private fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLoginTab.setOnClickListener {
            findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
        }
        binding.profileImageContainer.setOnClickListener {
            val options = arrayOf("Choose from Gallery", "Take a Photo")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Profile Image")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> pickImageLauncher.launch("image/*")
                        1 -> {
                            val imageFile = createImageFile(requireContext())
                            val uri = FileProvider.getUriForFile(
                                requireContext(),
                                requireContext().packageName + ".provider",
                                imageFile
                            )
                            cameraImageUri = uri
                            takePictureLauncher.launch(uri)
                        }
                    }
                }
                .show()
        }
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.progressRegister.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    binding.progressRegister.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    if (task.isSuccessful) {
                        // Extract username from email (before @)
                        val username = email.substringBefore("@")
                        // You can use 'username' as needed here (e.g., save to Firestore)
                        Toast.makeText(requireContext(), "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
                    } else {
                        Toast.makeText(requireContext(), "Registration failed: " + (task.exception?.localizedMessage ?: "Unknown error"), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
