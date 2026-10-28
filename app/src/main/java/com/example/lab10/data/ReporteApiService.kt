package com.example.lab10.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ReporteApiService {
    @GET("reporte")
    suspend fun selectReportes(): ArrayList<ReporteModel>

    @GET("reporte/{id}")
    suspend fun selectReporte(@Path("id") id: String): Response<ReporteModel>

    @Headers("Content-Type: application/json")
    @POST("reporte")
    suspend fun insertReporte(@Body reporte: ReporteModel): Response<ReporteModel>

    @PUT("reporte/{id}")
    suspend fun updateReporte(@Path("id") id: String, @Body reporte: ReporteModel): Response<ReporteModel>

    @DELETE("reporte/{id}")
    suspend fun deleteReporte(@Path("id") id: String): Response<Void>
}