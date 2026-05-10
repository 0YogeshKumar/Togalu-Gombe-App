package com.mindmatrix.togalugombe

data class Puppet(
    val id: String,
    val name_en: String,
    val name_kn: String,
    val description_en: String,
    val powers: String,
    val local_image: String // Changed from image_url
)