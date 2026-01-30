package com.example.androidsocialapp


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.androidsocialapp.databinding.FragmentFeedBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.example.androidsocialapp.ui.Post
import com.example.androidsocialapp.ui.PostAdapter

class FeedFragment : Fragment() {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val posts = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter

    // Loading state
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PostAdapter(posts)
        binding.feedRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter = adapter

        showLoading(true)
        fetchPostsFromFirestore()
    }

    private fun fetchPostsFromFirestore() {
        isLoading = true
        showLoading(true)
        db.collection("posts")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                posts.clear()
                for (document in result) {
                    val category = document.getString("category") ?: "General"
                    val content = document.getString("description") ?: ""
                    val createdAt = document.getTimestamp("createdAt")?.toDate()?.toString() ?: ""
                    val postedBy = document.getString("postedBy") ?: "Anonymous"
                    posts.add(Post(category, content, null, createdAt, postedBy))
                }
                adapter.notifyDataSetChanged()
                isLoading = false
                showLoading(false)
            }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(requireContext(), "Failed to load posts: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                isLoading = false
                showLoading(false)
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.feedRecyclerView.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
