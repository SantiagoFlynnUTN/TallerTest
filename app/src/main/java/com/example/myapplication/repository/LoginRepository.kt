package com.example.myapplication.repository

import kotlinx.coroutines.delay

class LoginRepository {
    suspend fun loginWithCredentials(username: String, password: String): Boolean {
        delay(2000)
        return username == "user" && password == "password"
    }
}