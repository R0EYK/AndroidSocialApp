package com.example.androidsocialapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidsocialapp.databinding.FragmentCreateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)

        setupCategorySelection()

        binding.btnSubmit.setOnClickListener {
            submitPost()
        }

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
                selectedCategory = category
            }
        }
    }

    private fun submitPost() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "You must be logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val category = selectedCategory
        val desc = binding.editDescription.text.toString().trim()

        if (category == null) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        if (desc.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        val authorName = user.displayName?.takeIf { it.isNotBlank() } ?: ""

        val post = hashMapOf(
            "category" to category.displayName,
            "description" to desc,
            "createdAt" to Date(),
            "postedByUid" to user.uid,
            "postedBy" to authorName
        )

        FirebaseFirestore.getInstance()
            .collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post submitted!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_createFragment_to_feedFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
