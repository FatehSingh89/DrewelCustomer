package com.os.drewel.apicall.responsemodel.applyloyaltypointresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("discount")
    @Expose
    var discount: Double? = null

}