package com.example.androidsocialapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.androidsocialapp.databinding.FragmentCreateBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.util.Date
import androidx.navigation.fragment.findNavController

class CreateFragment : Fragment() {
    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!

    private enum class Category(val displayName: String) {
        GENERAL("General"),
        ROAD_ASSIST("Road Assist"),
        MEDICAL("Medical"),
        GIVEAWAY("Giveaway")
    }

    private var selectedCategory: Category? = null
    private var selectedImageUri: android.net.Uri? = null
    private var cameraImageUri: android.net.Uri? = null

    private val pickImageLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.addPhotoIcon.setImageURI(it)
        }
    }

    private val takePictureLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && cameraImageUri != null) {
            selectedImageUri = cameraImageUri
            binding.addPhotoIcon.setImageURI(cameraImageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        setupCategorySelection()
        setupSubmitButton()
        setupPhotoPicker()
        return binding.root
    }

    private fun setupCategorySelection() {
        val categoryViews = listOf(
            Pair(Category.GENERAL, binding.categoryGeneral),
            Pair(Category.ROAD_ASSIST, binding.categoryRoadAssist),
            Pair(Category.MEDICAL, binding.categoryMedical),
            Pair(Category.GIVEAWAY, binding.categoryGiveaway)
        )
        for ((category, view) in categoryViews) {
            view.setOnClickListener {
                selectCategory(category)
            }
        }
    }

    private fun selectCategory(category: Category) {
        selectedCategory = category
        // Highlight selected, reset others
        val highlightBg = resources.getDrawable(com.example.androidsocialapp.R.drawable.btn_primary_bg, null)
        val categoryViews = listOf(
            Pair(Category.GENERAL, binding.categoryGeneral),
            Pair(Category.ROAD_ASSIST, binding.categoryRoadAssist),
            Pair(Category.MEDICAL, binding.categoryMedical),
            Pair(Category.GIVEAWAY, binding.categoryGiveaway)
        )
        for ((cat, view) in categoryViews) {
            view.background = if (cat == category) highlightBg else null
        }
    }

    private fun setupPhotoPicker() {
        binding.addPhotoContainer.setOnClickListener {
            val options = arrayOf("Choose from Gallery", "Take a Photo")
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Image")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> pickImageLauncher.launch("image/*")
                        1 -> {
                            val imageFile = createImageFile(requireContext())
                            val uri = androidx.core.content.FileProvider.getUriForFile(
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
    }

    private fun createImageFile(context: android.content.Context): java.io.File {
        val timeStamp: String = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val storageDir: java.io.File? = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return java.io.File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            val category = selectedCategory
            val description = binding.editDescription.text.toString().trim()
            if (category == null) {
                android.widget.Toast.makeText(requireContext(), "Please select a category", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Please enter a description", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firestore integration (skip image for now)
            val db = FirebaseFirestore.getInstance()
            val user = FirebaseAuth.getInstance().currentUser
            val post = hashMapOf(
                "category" to category.displayName,
                "description" to description,
                "createdAt" to Date(),
                "postedBy" to (user?.displayName ?: user?.email ?: user?.uid ?: "Anonymous")
            )
            db.collection("posts")
                .add(post)
                .addOnSuccessListener {
                    android.widget.Toast.makeText(requireContext(), "Post submitted!", android.widget.Toast.LENGTH_SHORT).show()
                    // Navigate to FeedFragment using the child NavController
                    androidx.navigation.Navigation.findNavController(requireView())
                        .navigate(com.example.androidsocialapp.R.id.action_createFragment_to_feedFragment)
                }
                .addOnFailureListener { e ->
                    android.widget.Toast.makeText(requireContext(), "Failed to submit: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
