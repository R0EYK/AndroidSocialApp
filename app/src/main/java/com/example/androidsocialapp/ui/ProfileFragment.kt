package com.example.androidsocialapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import com.example.androidsocialapp.R

class ProfileFragment : Fragment() {
        private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        auth = FirebaseAuth.getInstance()

        // Set profile name from FirebaseAuth user
        val profileNameText = view.findViewById<android.widget.TextView>(R.id.profileName)
        val user = auth.currentUser
        val displayName = user?.displayName
        val email = user?.email
        val uid = user?.uid
        profileNameText.text = when {
            !displayName.isNullOrBlank() -> displayName
            !email.isNullOrBlank() -> email
            else -> uid ?: "Unknown User"
        }

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            // Use the main NavController from the activity's fragment container view
            val mainNavController = androidx.navigation.Navigation.findNavController(requireActivity(), com.example.androidsocialapp.R.id.nav_host_fragment)
            mainNavController.popBackStack(com.example.androidsocialapp.R.id.loginFragment, false)
        }

        val postsContainer = view.findViewById<LinearLayout>(R.id.postsContainer)
        val posts = listOf(
            Triple("General", "This is a general post.", com.example.androidsocialapp.R.drawable.ic_general),
            Triple("Road Assist", "Need help with a flat tire!", com.example.androidsocialapp.R.drawable.ic_road_assist),
            Triple("Medical", "Looking for a nearby pharmacy.", com.example.androidsocialapp.R.drawable.ic_medical),
            Triple("Giveaway", "Giving away a desk, free pickup!", com.example.androidsocialapp.R.drawable.ic_giveaway)
        )
        val createdAt = "Just now"
        val postedBy = profileNameText.text.toString()
        for ((category, content, iconRes) in posts) {
            val postCard = inflater.inflate(com.example.androidsocialapp.R.layout.view_post_card, postsContainer, false)
            val categoryIcon = postCard.findViewById<android.widget.ImageView>(com.example.androidsocialapp.R.id.categoryIcon)
            val categoryText = postCard.findViewById<android.widget.TextView>(com.example.androidsocialapp.R.id.categoryText)
            val postContent = postCard.findViewById<android.widget.TextView>(com.example.androidsocialapp.R.id.postContent)
            val createdAtText = postCard.findViewById<android.widget.TextView>(com.example.androidsocialapp.R.id.createdAt)
            val postedByText = postCard.findViewById<android.widget.TextView>(com.example.androidsocialapp.R.id.postedBy)
            categoryIcon.setImageResource(iconRes)
            categoryText.text = category
            postContent.text = content
            createdAtText.text = createdAt
            postedByText.text = "Posted by $postedBy"
            postsContainer.addView(postCard)
        }
        return view
    }
}
