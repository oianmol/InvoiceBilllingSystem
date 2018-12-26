package com.dashlabs.invoicemanagement.view.products

import tornadofx.*

class Product {
    var name by property<String>()
    fun nameProperty() = getProperty(Product::name)

    var search by property<String>()
    fun searchProperty() = getProperty(Product::search)

    var amount by property<Number>()
    fun amountProperty() = getProperty(Product::amount)

    override fun toString() = name
}