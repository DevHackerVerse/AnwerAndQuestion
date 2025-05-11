package com.example.anwerandquestion.model

class User {
    var uid:String? = null
    var name:String?= null
    var mobileNumber:String? = null
    var profileImage: String? = null
    constructor(){}
    constructor(
        uid:String?,
        name:String?,
        mobileNumber:String?,
        profileImage:String?
    ){
        this.uid = uid
        this.name = name
        this.mobileNumber = mobileNumber
        this.profileImage = profileImage
    }
}