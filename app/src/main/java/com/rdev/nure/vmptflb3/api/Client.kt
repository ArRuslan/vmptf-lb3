package com.rdev.nure.vmptflb3.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Client {
    companion object {
        private var client: Retrofit? = null

        fun getClient(): Retrofit {
            if(client == null)
                client = Retrofit.Builder()
                    .baseUrl("http://192.168.0.111:3000")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            return client!!
        }
    }
}