package com.nadzira.storyapp.remote.retrofit

import com.nadzira.storyapp.remote.response.DetailResponse
import com.nadzira.storyapp.remote.response.LoginResponse
import com.nadzira.storyapp.remote.response.RegisterResponse
import com.nadzira.storyapp.remote.response.StoryResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories")
    suspend fun getStories(
    ): StoryResponse

    @GET("events/{id}")
    fun getDetailStory(
        @Path("id") storyId: String
    ): DetailResponse
}