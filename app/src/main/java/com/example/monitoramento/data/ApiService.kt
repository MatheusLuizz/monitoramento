package com.example.monitoramento.data

import retrofit2.http.GET

interface ApiService {

    @GET("/api/data")
    suspend fun getData(): List<Data>
}