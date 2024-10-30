package com.nadzira.storyapp.ui

data class UserModel(
    val email: String,
    val token: String,
    val isLogin: Boolean = false
)