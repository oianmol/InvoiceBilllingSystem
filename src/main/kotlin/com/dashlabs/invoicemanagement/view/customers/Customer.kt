package com.dashlabs.invoicemanagement.view.customers

import tornadofx.*

class Customer {
    var name by property<String>()
    fun nameProperty() = getProperty(Customer::name)

    var identity by property<String>()
    fun getIdentityProperty() = getProperty(Customer::identity)

    var search by property<String>()
    fun searchProperty() = getProperty(Customer::search)

    var address by property<String>()
    fun addressProperty() = getProperty(Customer::address)

    var mobileno by property<String>()
    fun mobileProperty() = getProperty(Customer::mobileno)

    var district by property<String>()
    fun districtProperty() = getProperty(Customer::district)

    var state by property<String>()
    fun stateProperty() = getProperty(Customer::state)

    override fun toString() = name
}