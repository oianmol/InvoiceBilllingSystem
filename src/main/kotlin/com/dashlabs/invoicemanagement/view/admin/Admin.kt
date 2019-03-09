package com.dashlabs.invoicemanagement.view.admin

import tornadofx.*

class Admin {

    constructor(name:String?=null,password:String?=null){
        this.username = name
        this.password = password
    }
    var username by property<String>()
    fun nameProperty() = getProperty(Admin::username)

    var password by property<String>()
    fun passwordProperty() = getProperty(Admin::password)


    var newpassword by property<String>()
    fun newpasswordProperty() = getProperty(Admin::newpassword)

    override fun toString() = username
}