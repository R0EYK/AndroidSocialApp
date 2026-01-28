package com.example.androidsocialapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.androidsocialapp.R

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val postsContainer = view.findViewById<LinearLayout>(R.id.postsContainer)

        val posts = listOf(
            Triple("General", "This is a general post.", R.drawable.ic_general),
            Triple("Road Assist", "Need help with a flat tire!", R.drawable.ic_road_assist),
            Triple("Medical", "Looking for a nearby pharmacy.", R.drawable.ic_medical),
            Triple("Giveaway", "Giving away a desk, free pickup!", R.drawable.ic_giveaway)
        )

        val createdAt = "Just now"
        val postedBy = "Patrick Star"

        for ((category, content, iconRes) in posts) {
            val postCard = inflater.inflate(R.layout.view_post_card, postsContainer, false)
            val categoryIcon = postCard.findViewById<android.widget.ImageView>(R.id.categoryIcon)
            val categoryText = postCard.findViewById<android.widget.TextView>(R.id.categoryText)
            val postContent = postCard.findViewById<android.widget.TextView>(R.id.postContent)
            val createdAtText = postCard.findViewById<android.widget.TextView>(R.id.createdAt)
            val postedByText = postCard.findViewById<android.widget.TextView>(R.id.postedBy)

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
