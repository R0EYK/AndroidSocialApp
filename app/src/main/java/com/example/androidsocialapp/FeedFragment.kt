package com.example.androidsocialapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.androidsocialapp.databinding.FragmentFeedBinding

class FeedFragment : Fragment() {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val posts = listOf(
            com.example.androidsocialapp.ui.Post("General", "This is a general post.", null, "1 min ago", "Sandy Cheeks"),
            com.example.androidsocialapp.ui.Post("Road Assist", "Need help with a flat tire.", com.example.androidsocialapp.R.drawable.sample_image, "5 min ago", "Patrick Star"),
            com.example.androidsocialapp.ui.Post("Medical", "Anyone know a good dentist?", null, "10 min ago", "Squidward Tentacles"),
            com.example.androidsocialapp.ui.Post("Giveaway", "Giving away free cookies!", com.example.androidsocialapp.R.drawable.sample_image, "15 min ago", "Mr. Krabs")
        )

        val adapter = com.example.androidsocialapp.ui.PostAdapter(posts)
        binding.feedRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
