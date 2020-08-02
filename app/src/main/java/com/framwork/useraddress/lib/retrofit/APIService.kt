package com.framwork.useraddress.lib.retrofit


import com.framwork.useraddress.lib.base.AddressData
import com.framwork.useraddress.lib.base.SuccessData
import com.framwork.useraddress.lib.utils.Constants
import com.framwork.useraddress.model.UserData
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * @author Aditi Shirsat
 */
interface APIService {

    /**
     * Get the instance of Base URL.
     */
    companion object {
        fun getBaseUrl(): APIService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .client(getClient())
                .baseUrl(Constants.BASEURL)
                .build()
            return retrofit.create(APIService::class.java)
        }

        /**
         * @return Instance of OkHttpClient class with modified timeout
         */
        private fun getClient(): OkHttpClient {
            val httpTimeout: Long = 80
            val okHttpClientBuilder = OkHttpClient.Builder()
            okHttpClientBuilder.connectTimeout(httpTimeout, TimeUnit.SECONDS)
            okHttpClientBuilder.readTimeout(httpTimeout, TimeUnit.SECONDS)
            return okHttpClientBuilder.build()
        }
    }

    /**
     * API
     */

    //127.0.0.8000/api/login
    @FormUrlEncoded
    @POST("login/")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<UserData>

    //127.0.0.8000/api/register
    @FormUrlEncoded
    @POST("register/")
    fun register(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<UserData>

    //127.0.0.8000/api/save_address
    @FormUrlEncoded
    @POST("save_address/")
    fun saveAddress(
        @Field("address_line1") address_line1: String,
        @Field("address_line2") address_line2: String,
        @Field("city") city: String,
        @Field("pin_code") pin_code: String,
        @Field("user_id") user_id: String
    ): Call<SuccessData>

    //127.0.0.8000/api/login
    @FormUrlEncoded
    @POST("get_address/")
    fun getAddress(
        @Field("user_id") user_id: String
    ): Call<AddressData>
}
