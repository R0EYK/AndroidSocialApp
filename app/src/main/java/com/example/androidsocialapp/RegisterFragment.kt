package com.example.androidsocialapp

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidsocialapp.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfileImage.setImageURI(it)
            binding.llImageOverlay.visibility = View.GONE
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && cameraImageUri != null) {
            selectedImageUri = cameraImageUri
            binding.ivProfileImage.setImageURI(cameraImageUri)
            binding.llImageOverlay.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
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
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter name, email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (!task.isSuccessful) {
                        setLoading(false)
                        Toast.makeText(requireContext(), "Registration failed: " + (task.exception?.localizedMessage ?: "Unknown error"), Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    val user = auth.currentUser
                    if (user == null) {
                        setLoading(false)
                        Toast.makeText(requireContext(), "Registration failed: user is null", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    uploadProfileImageIfNeeded(user.uid) { photoUrl ->
                        val reqBuilder = UserProfileChangeRequest.Builder().setDisplayName(name)
                        if (!photoUrl.isNullOrBlank()) reqBuilder.setPhotoUri(Uri.parse(photoUrl))

                        user.updateProfile(reqBuilder.build())
                            .addOnSuccessListener {
                                user.reload().addOnCompleteListener {
                                    setLoading(false)
                                    Toast.makeText(requireContext(), "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
                                }
                            }
                            .addOnFailureListener { e ->
                                setLoading(false)
                                Toast.makeText(requireContext(), "Profile update failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressRegister.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
        binding.btnLoginTab.isEnabled = !loading
        binding.profileImageContainer.isEnabled = !loading
        binding.etName.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
    }

    private fun uploadProfileImageIfNeeded(uid: String, cb: (String?) -> Unit) {
        val uri = selectedImageUri
        if (uri == null) {
            cb(null)
            return
        }

        val ref = FirebaseStorage.getInstance().reference.child("profileImages/$uid.jpg")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { downloadUri: Uri -> cb(downloadUri.toString()) }
                    .addOnFailureListener { cb(null) }
            }
            .addOnFailureListener { cb(null) }
    }

    private fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
