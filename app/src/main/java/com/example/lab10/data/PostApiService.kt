package com.example.lab10.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PostApiService {
    @GET("posts")
    suspend fun selectPosts(): ArrayList<PostModel>

    @GET("posts/{id}")
    suspend fun selectPost(@Path("id") id: String): Response<PostModel>

    @Headers("Content-Type: application/json")
    @POST("posts")
    suspend fun insertPost(@Body post: PostModel): Response<PostModel>

    @PUT("posts/{id}")
    suspend fun updatePost(@Path("id") id: String, @Body post: PostModel): Response<PostModel>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Void>
}