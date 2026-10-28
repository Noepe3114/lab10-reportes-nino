package com.example.lab10.data

import com.google.gson.annotations.SerializedName

data class ReporteModel(
    @SerializedName("id")
    var id: Int,
    @SerializedName("title")
    var title: String,
    @SerializedName("description")
    var description: String,
    @SerializedName("location")
    var location: String,
    @SerializedName("event_type")
    var event_type: String,
    @SerializedName("severity")
    var severity: String,
    @SerializedName("report_date")
    var report_date: String,
    @SerializedName("status")
    var status: String
)