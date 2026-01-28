package com.example.androidsocialapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.androidsocialapp.R

class PostDetailFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_detail, container, false)

        // Toolbar setup
        val backArrow = view.findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        val titleText = view.findViewById<TextView>(R.id.titleText)
        titleText.text = "Post Details"

        // Post card content setup (find inside included layout)
        val categoryIcon = view.findViewById<ImageView>(R.id.categoryIcon)
        val categoryText = view.findViewById<TextView>(R.id.categoryText)
        val postContent = view.findViewById<TextView>(R.id.postContent)
        val postImage = view.findViewById<ImageView>(R.id.postImage)
        val createdAtText = view.findViewById<TextView>(R.id.createdAt)
        val postedByText = view.findViewById<TextView>(R.id.postedBy)

        val args = requireArguments()
        val category = args.getString("category") ?: ""
        val content = args.getString("content") ?: ""
        val createdAt = args.getString("createdAt") ?: ""
        val postedBy = args.getString("postedBy") ?: ""
        val imageRes = args.getInt("imageRes", 0)

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

        if (imageRes != 0) {
            postImage.setImageResource(imageRes)
            postImage.visibility = View.VISIBLE
        } else {
            postImage.visibility = View.GONE
        }

        return view
    }
}
