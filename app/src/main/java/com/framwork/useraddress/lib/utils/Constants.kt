package com.framwork.useraddress.lib.utils

import com.framwork.useraddress.model.UserData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * Constants class is used to store all constant data used in application.
 *
 * @author Aditi Shirsat
 */
object Constants {

    var user_id = ""

    /*
     * @return User Id
     */
    fun getUserId(): String {
         return  user_id ?:""
    }

    fun setUserId(user_id: String){
        this.user_id = user_id
    }
    /**
     * @return User Private key
     */
    /*fun getPrivateKey(): String {
        return getUserData(). ?: ""
    }*/

    /**
     * @return JWT Token
     *//*
    fun getToken(): String {
        return Gson().fromJson(Utility.getPreference(PREF_USER_DATA), UserData::class.java)?.token?:""
    }*/

/*    *//**
     * @return All user data
     *//*
    fun getUserData(): UserData {
        return Gson().fromJson(Utility.getPreference(PREF_USER_DATA), UserData::class.java) ?: UserData()
    }*/

    /**
     * @return All user data
     */
    /*fun getAssociateTypeId(): String {
        return getUserData().associateTypeId ?: ""
    }*/

    /**
     * @param key To retrieve particular message
     * @return Error message
     */
    fun getLocalErrorMsg(key: String): String {
        return try {
            val errorMap: HashMap<String, String> = Gson().fromJson(
                Utility.retrieveFileContent(FILE_LOCAL_ERRORS),
                object : TypeToken<HashMap<String, String>>() {}.type
            )

            if (errorMap.containsKey(key))
                errorMap[key].toString()
            else
                "XXX"

        } catch (e: Exception) {
            "XXX"
        }
    }

    /**
     * @param errorCode This is errorCode returned by API.
     * @return Error message( Mapped from API)
     */
/*
    fun getServerErrorMsg(errorCode: String): String {
        try {
            val errorList: ArrayList<Errors> = Gson().fromJson(
                Utility.retrieveFileContent(FILE_SERVER_ERRORS),
                object : TypeToken<ArrayList<Errors>>() {}.type
            )

            if (errorList != null && errorList.size > 0) {
                for (error in errorList) {
                    if (error.errorCode == errorCode)
                        return error.title ?:""
                }
            } else
                return "XXX"

        } catch (e: Exception) {
            return "XXX"
        }
        return "XXX"
    }
*/


    const val ERROR = "Error!"
    const val WARNING = "Warning!"
    const val SUCCESS = "Success!"
    /*Shared Preferences Keys*/
    const val PREF_USER_DATA = "UserData"

    /*Types of User*/
    const val USER_EMPLOYEE = "1"
    const val USER_PREF_ASSOCIATE = "2"
    const val USER_ASSOCIATE = "3"

    /*Hardcoded Values*/
    const val DEVICE_TYPE_ID = "A"
    const val USER_TYPE_ID = "2"
    const val SECURITY_TOKEN = "no token"

    /* General Flags*/
    const val FLAG_TRUE = "1"
    const val FLAG_FALSE = "0"
    const val FLAG_MALE = "1"
    const val FLAG_FEMALE = "2"

   /*Language*/
   const val LANGUAGE_ENGLISH = "en"
   const val LANGUAGE_AREBIC = "ar"

    /*Animation File Names*/
    const val JSON_NO_INTERNET = "no_internet.json"
    const val JSON_NO_POST = "no_post.json"
    const val JSON_SERVER_ERROR = "server_error.json"

    /*Cache File Names*/
    const val FILE_LOCAL_ERRORS = "LocalErrors.txt"
    const val FILE_SERVER_ERRORS = "ServerErrors.txt"

    // Error constants.
    const val _111 = "111"

    /*Component Constants*/
    const val COMPONENT_TIMELINE: String = "timeLineComponent"
    const val COMPONENT_BANNER: String = "banerComponent"
    const val COMPONENT_CATEGORY: String = "categoryComponent"
    const val COMPONENT_CATEGORY_ITEM: String = "categoryItemComponent"
    const val COMPONENT_EVENT: String = "eventComponent"
    const val COMPONENT_INVITE = "inviteComponent"



    const val BASEURL = "https://frozen-stream-02844.herokuapp.com/api/"
}
