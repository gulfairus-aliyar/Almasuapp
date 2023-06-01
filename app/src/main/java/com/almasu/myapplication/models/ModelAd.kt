package com.almasu.myapplication.models

class ModelAd {

    var id = ""
    var uid: String = ""
    var nameService: String = ""
    var category: String = ""
    var address: String = ""
    var description: String = ""
    var status: String = ""
    var timestamp: Long = 0
    var latitude = 0.0
    var longitude = 0.0
    var userName: String = ""
    var profileImageUrl: String = ""

    constructor()
    constructor(
        id: String,
        uid: String,
        nameService: String,
        category: String,
        address: String,
        description: String,
        status: String,
        timestamp: Long,
        latitude: Double,
        longitude: Double,
        userName: String,
        profileImageUrl: String
    ) {
        this.id = id
        this.uid = uid
        this.nameService = nameService
        this.category = category
        this.address = address
        this.description = description
        this.status = status
        this.timestamp = timestamp
        this.latitude = latitude
        this.longitude = longitude
        this.userName = userName
        this.profileImageUrl = profileImageUrl
    }


}