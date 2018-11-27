package com.dashlabs.invoicemanagement.view.products

import tornadofx.*

class Product {
    constructor(name: String? = null, section: String? = null) {
        this.name = name
        this.section = section
    }

    var name by property<String>()
    fun nameProperty() = getProperty(Product::name)

    var section by property<String>()
    fun sectionProperty() = getProperty(Product::section)

    override fun toString() = name
}