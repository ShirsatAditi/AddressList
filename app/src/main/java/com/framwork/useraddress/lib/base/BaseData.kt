package com.framwork.useraddress.lib.base

import com.google.gson.annotations.SerializedName

/**
 * BaseData
 *
 * @author Aditi Shirsat
 */

abstract class BaseData(
    @SerializedName("Message", alternate = ["message"]) val message: String? = "",
    @SerializedName("Status", alternate = ["status"]) val status: String? = "",
    val errorCode: String? = ""
)

class SuccessData():BaseData()

class AddressData(
    @SerializedName("address_list") val addressList:ArrayList<AddressList>?= arrayListOf()
):BaseData()

class AddressList(
    @SerializedName("id") val id:String?="",
    @SerializedName("address_line1") val address_line1:String?="",
    @SerializedName("address_line2") val address_line2:String?="",
    @SerializedName("city") val city:String?="",
    @SerializedName("pin_code") val pin_code:String?=""
)

