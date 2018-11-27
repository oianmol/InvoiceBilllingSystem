package com.dashlabs.invoicemanagement.view.products

import tornadofx.*

class Product {
    var name by property<String>()
    fun nameProperty() = getProperty(Product::name)

    var search by property<String>()
    fun searchProperty() = getProperty(Product::search)

    var section by property<String>()
    fun sectionProperty() = getProperty(Product::section)

    override fun toString() = name
}