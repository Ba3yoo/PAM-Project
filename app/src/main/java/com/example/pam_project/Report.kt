package com.example.pam_project

class Report {
    var id: String? = null
    var title: String? = null
    var description:String? = null
    var imageUrl: String? = null
    constructor() {}
    constructor(title: String?, description: String?, imageUrl: String?) {
        this.title = title
        this.description = description
        this.imageUrl = imageUrl
    }
}