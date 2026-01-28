package com.example.androidsocialapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.androidsocialapp.R

class PostCardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.view_post_card, container, false)

        // Static data for now
        val category = "Road Assist"
        val content = "TEST TEST TEST"
        val createdAt = "Just now"
        val postedBy = "Patrick Star"
        val hasImage = false // Set to true to show image

        val categoryIcon = view.findViewById<ImageView>(R.id.categoryIcon)
        val categoryText = view.findViewById<TextView>(R.id.categoryText)
        val postContent = view.findViewById<TextView>(R.id.postContent)
        val postImage = view.findViewById<ImageView>(R.id.postImage)
        val createdAtText = view.findViewById<TextView>(R.id.createdAt)
        val postedByText = view.findViewById<TextView>(R.id.postedBy)

        // Set category icon based on category
        val iconRes = when (category) {
            "General" -> R.drawable.ic_general
            "Road Assist" -> R.drawable.ic_road_assist
            "Medical" -> R.drawable.ic_medical
            "Giveaway" -> R.drawable.ic_giveaway
            else -> R.drawable.ic_general
        }
        categoryIcon.setImageResource(iconRes)
        categoryText.text = category

        postContent.text = content
        createdAtText.text = createdAt
        postedByText.text = "Posted by $postedBy"

        postImage.visibility = if (hasImage) View.VISIBLE else View.GONE

        return view
    }
}
