package com.example.androidsocialapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidsocialapp.R

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryIcon: ImageView = view.findViewById(R.id.categoryIcon)
        val categoryText: TextView = view.findViewById(R.id.categoryText)
        val postContent: TextView = view.findViewById(R.id.postContent)
        val postImage: ImageView = view.findViewById(R.id.postImage)
        val createdAtText: TextView = view.findViewById(R.id.createdAt)
        val postedByText: TextView = view.findViewById(R.id.postedBy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_post_card, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val iconRes = when (post.category) {
            "General" -> R.drawable.ic_general
            "Road Assist" -> R.drawable.ic_road_assist
            "Medical" -> R.drawable.ic_medical
            "Giveaway" -> R.drawable.ic_giveaway
            else -> R.drawable.ic_general
        }
        holder.categoryIcon.setImageResource(iconRes)
        holder.categoryText.text = post.category
        holder.postContent.text = post.content
        holder.createdAtText.text = post.createdAt
        holder.postedByText.text = "Posted by ${post.postedBy}"

        if (post.imageRes != null) {
            holder.postImage.setImageResource(post.imageRes)
            holder.postImage.visibility = View.VISIBLE
        } else {
            holder.postImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val fragment = PostDetailFragment()
            val args = android.os.Bundle().apply {
                putString("category", post.category)
                putString("content", post.content)
                putString("createdAt", post.createdAt)
                putString("postedBy", post.postedBy)
                post.imageRes?.let { putInt("imageRes", it) }
            }
            fragment.arguments = args

            val activity = holder.itemView.context as? androidx.fragment.app.FragmentActivity
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(
                    com.example.androidsocialapp.R.id.nav_host_fragment,
                    fragment
                )
                ?.addToBackStack(null)
                ?.commit()
        }
    }

    override fun getItemCount() = posts.size
}
