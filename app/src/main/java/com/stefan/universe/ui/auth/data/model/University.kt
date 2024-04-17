package com.stefan.universe.ui.auth.data.model

data class University(
    val name: String,
    val emailIdentifier: List<String>,
    val location: String,
    val faculties: List<String>
)