package com.marcoshier.types

import kotlinx.serialization.Serializable

@Serializable
data class Data (
    val projects: List<Project>,
    val authors: List<Author>,
    val categories: List<Category>
)

@Serializable
data class Project(
    val name: String,
    val year: Int? = null,
    val authors: List<Int>,
    val categories: List<Int>,
    val partner: String,
    val location: String
)

@Serializable
data class Author(
    val name: String,
    val projects: List<Int>
)

@Serializable
data class Category(
    val name: String,
    val projects: List<Int>
)