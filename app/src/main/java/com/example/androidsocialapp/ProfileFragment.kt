package com.example.androidsocialapp

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidsocialapp.databinding.FragmentProfileBinding
import com.example.androidsocialapp.ui.Post
import com.example.androidsocialapp.ui.PostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val posts = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter

    private var selectedPhotoUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedPhotoUri = uri
                binding.profileImage.setImageURI(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        setupRecycler()
        renderUser()
        loadMyPosts()

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            androidx.navigation.Navigation
                .findNavController(requireActivity(), R.id.nav_host_fragment)
                .navigate(R.id.loginFragment)
        }

        binding.btnEditProfile.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            binding.editSection.visibility = View.VISIBLE
            binding.btnEditProfile.visibility = View.GONE
            binding.etEditName.setText(user?.displayName ?: "")
        }

        binding.btnCancelEdit.setOnClickListener {
            binding.editSection.visibility = View.GONE
            binding.btnEditProfile.visibility = View.VISIBLE
            selectedPhotoUri = null
            renderUser()
        }

        binding.btnPickPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        return binding.root
    }

    private fun setupRecycler() {
        adapter = PostAdapter(posts)
        binding.profilePostsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.profilePostsRecyclerView.adapter = adapter
    }

    private fun renderUser() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            binding.profileName.text = ""
            binding.profileEmail.text = ""
            binding.profileImage.setImageResource(R.drawable.profile_example_pic)
            return
        }

        binding.profileName.text = user.displayName ?: ""
        binding.profileEmail.text = user.email ?: ""

        val photo = user.photoUrl
        if (photo != null) {
            binding.profileImage.setImageURI(photo)
        } else {
            binding.profileImage.setImageResource(R.drawable.profile_example_pic)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressProfile.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSaveProfile.isEnabled = !loading
        binding.btnCancelEdit.isEnabled = !loading
        binding.btnPickPhoto.isEnabled = !loading
        binding.etEditName.isEnabled = !loading
    }

    private fun saveProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val newName = binding.etEditName.text.toString().trim()
        if (newName.isBlank()) {
            Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        uploadProfilePhotoIfNeeded(user.uid) { photoUrl ->
            val reqBuilder = UserProfileChangeRequest.Builder().setDisplayName(newName)
            if (!photoUrl.isNullOrBlank()) reqBuilder.setPhotoUri(Uri.parse(photoUrl))

            user.updateProfile(reqBuilder.build())
                .addOnSuccessListener {
                    user.reload()
                        .addOnCompleteListener {
                            updateAllMyPostsAuthorName(user.uid, newName) { ok ->
                                setLoading(false)

                                if (!ok) {
                                    Toast.makeText(requireContext(), "Profile updated, posts update failed", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                                }

                                binding.editSection.visibility = View.GONE
                                binding.btnEditProfile.visibility = View.VISIBLE
                                renderUser()
                                loadMyPosts()
                            }
                        }
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(requireContext(), "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun uploadProfilePhotoIfNeeded(uid: String, cb: (String?) -> Unit) {
        val uri = selectedPhotoUri
        if (uri == null) {
            cb(null)
            return
        }

        val ref = FirebaseStorage.getInstance().reference
            .child("profileImages/$uid.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { downloadUri: Uri -> cb(downloadUri.toString()) }
                    .addOnFailureListener { cb(null) }
            }
            .addOnFailureListener { cb(null) }
    }

    private fun updateAllMyPostsAuthorName(uid: String, newName: String, cb: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereEqualTo("postedByUid", uid)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    cb(true)
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                for (doc in snap.documents) {
                    batch.update(doc.reference, mapOf("postedBy" to newName))
                }

                batch.commit()
                    .addOnSuccessListener { cb(true) }
                    .addOnFailureListener { cb(false) }
            }
            .addOnFailureListener { cb(false) }
    }

    private fun loadMyPosts() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        posts.clear()

        db.collection("posts")
            .whereEqualTo("postedByUid", user.uid)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val category = doc.getString("category") ?: "Unknown"
                    val content = doc.getString("description") ?: ""
                    val createdAt = doc.getTimestamp("createdAt")?.toDate()?.toString() ?: ""
                    val postedBy = doc.getString("postedBy") ?: ""
                    posts.add(Post(category, content, null, createdAt, postedBy))
                }

                binding.postsCount.text = "${posts.size} Posts"
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                binding.postsCount.text = "0 Posts"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
