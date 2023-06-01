package com.almasu.myapplication.models

class ModelUsers {

    //variables, must match as in firebase
    var uid: String = ""
    var name: String = ""
    var email: String = ""
    var search: String = ""
    var phoneCode: String = ""
    var phoneNumber: String = ""
    var profileImageUrl: String = ""
    var userType: String = ""
    var timestamp: Long = 0

    //empty constructor, required by firebase
    constructor()

    //parametrized constructor
    constructor(
        uid: String,
        name: String,
        email: String,
        search: String,
        phoneCode: String,
        phoneNumber: String,
        profileImageUrl: String,
        userType: String,
        timestamp: Long
    ) {
        this.uid = uid
        this.name = name
        this.email = email
        this.search = search
        this.phoneCode = phoneCode
        this.phoneNumber = phoneNumber
        this.profileImageUrl = profileImageUrl
        this.userType = userType
        this.timestamp = timestamp
    }


}