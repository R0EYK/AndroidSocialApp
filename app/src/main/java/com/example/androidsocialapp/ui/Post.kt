package com.example.androidsocialapp.ui

data class Post(
    val category: String,
    val content: String,
    val imageRes: Int?,
    val createdAt: String,
    val postedBy: String
)
