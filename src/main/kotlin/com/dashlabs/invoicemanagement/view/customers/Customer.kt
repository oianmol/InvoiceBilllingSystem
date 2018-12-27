package com.dashlabs.invoicemanagement.view.customers

import tornadofx.*

class Customer {
    var name by property<String>()
    fun nameProperty() = getProperty(Customer::name)

    var identity by property<String>()
    fun getIdentityProperty() = getProperty(Customer::identity)

    var search by property<String>()
    fun searchProperty() = getProperty(Customer::search)

    var balance by property<Number>()
    fun balanceProperty() = getProperty(Customer::balance)

    var age by property<Number>()
    fun ageProperty() = getProperty(Customer::age)

    var aadhar by property<Number>()
    fun aadharProperty() = getProperty(Customer::aadhar)

    var district by property<Int>()
    fun districtProperty() = getProperty(Customer::district)

    var village by property<Int>()
    fun villageProperty() = getProperty(Customer::village)

    var state by property<Int>()
    fun stateProperty() = getProperty(Customer::state)

    override fun toString() = name
}