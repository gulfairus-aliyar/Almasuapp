package com.almasu.myapplication.models

class ModelMessage {

    var messageId: String? = null
    var message: String? = null
    var senderId: String? = null
    var imageUrl: String? = null
    var timeStamp: Long = 0

    constructor()
    constructor(
        messageId: String?,
        message: String?,
        senderId: String?,
        imageUrl: String?,
        timeStamp: Long
    ) {
        this.messageId = messageId
        this.message = message
        this.senderId = senderId
        this.imageUrl = imageUrl
        this.timeStamp = timeStamp
    }
}