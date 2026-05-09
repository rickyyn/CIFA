package br.edu.fatecpg.cifa.cloudinary

import com.cloudinary.Cloudinary

object CloudinaryConfig {

    val cloudinary: Cloudinary by lazy {

        val config = mapOf(
            "cloud_name" to "daintsptf",
            "api_key" to "782347127457419",
            "api_secret" to "P4qDufXqXSJxHJZ6VUEUnvHWfJM"
        )

        Cloudinary(config)
    }
}