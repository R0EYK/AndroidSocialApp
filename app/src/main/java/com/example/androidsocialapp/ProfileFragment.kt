package com.example.androidsocialapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidsocialapp.ui.Post
import com.example.androidsocialapp.ui.PostAdapter
import androidx.fragment.app.Fragment
import com.example.androidsocialapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {
	private var _binding: FragmentProfileBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentProfileBinding.inflate(inflater, container, false)
		setProfileInfo()
		setupRecyclerView()
		fetchAndDisplayUserPosts()

		binding.logoutButton.setOnClickListener {
			FirebaseAuth.getInstance().signOut()
			val navController = androidx.navigation.Navigation.findNavController(requireActivity(), com.example.androidsocialapp.R.id.nav_host_fragment)
			navController.setGraph(com.example.androidsocialapp.R.navigation.nav_graph)
			navController.navigate(com.example.androidsocialapp.R.id.loginFragment)
		}

		return binding.root
	}

	private fun setProfileInfo() {
		val user = FirebaseAuth.getInstance().currentUser
		val displayName = when {
			!user?.displayName.isNullOrBlank() -> user?.displayName!!
			!user?.email.isNullOrBlank() -> user?.email!!
			!user?.uid.isNullOrBlank() -> user?.uid!!
			else -> "Anonymous"
		}
		binding.profileName.text = displayName
	}

		private val userPosts = mutableListOf<Post>()
		private lateinit var postAdapter: PostAdapter

		private fun setupRecyclerView() {
			postAdapter = PostAdapter(userPosts)
			binding.profilePostsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
			binding.profilePostsRecyclerView.adapter = postAdapter
		}

		private fun fetchAndDisplayUserPosts() {
			val user = FirebaseAuth.getInstance().currentUser
			val email = user?.email ?: return
			val db = FirebaseFirestore.getInstance()
			userPosts.clear()
			android.util.Log.d("ProfileFragment", "Fetching posts for postedBy: $email")
			db.collection("posts")
				.whereEqualTo("postedBy", email)
				.get()
				.addOnSuccessListener { result ->
					android.util.Log.d("ProfileFragment", "Fetched ${result.size()} posts for $email")
					binding.postsCount.text = "${result.size()} Posts"
					if (result.isEmpty) {
						// Optionally show empty state
					} else {
						for (doc in result) {
							val category = doc.getString("category") ?: "Unknown"
							val content = doc.getString("description") ?: "No description"
							val createdAt = doc.getTimestamp("createdAt")?.toDate()?.toString() ?: ""
							val postedBy = doc.getString("postedBy") ?: ""
							userPosts.add(Post(category, content, null, createdAt, postedBy))
						}
					}
					postAdapter.notifyDataSetChanged()
				}
				.addOnFailureListener { e ->
					android.util.Log.e("ProfileFragment", "Failed to load posts: ${e.localizedMessage}")
					// Optionally show error state
				}
		}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}

