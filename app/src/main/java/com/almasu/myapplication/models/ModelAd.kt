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
    var username: String = ""
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
        username: String,
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
        this.username = username
        this.profileImageUrl = profileImageUrl
    }


}