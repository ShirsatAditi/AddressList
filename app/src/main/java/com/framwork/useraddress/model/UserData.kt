package com.framwork.useraddress.model

import com.framwork.useraddress.lib.base.BaseData
import com.google.gson.annotations.SerializedName

/**
 * UserData
 *
 * @author Aditi Shirsat
 */
object  UserData : BaseData(){
    private var instance : UserData? = null

    fun setInstance(instance:UserData){
        this.instance = instance
    }

    fun getInstance() : UserData?{
        return instance
    }

    @SerializedName("user_data")
    var userData: UserDetailsData? = UserDetailsData()

    class UserDetailsData {
        @SerializedName("token")
        val token: String? = ""

        @SerializedName("user_id")
        val user_id: String? = ""

        @SerializedName("email")
        val email: String? = ""

        @SerializedName("username")
        val username: String? = ""
    }
}