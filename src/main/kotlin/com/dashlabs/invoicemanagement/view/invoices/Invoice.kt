package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import tornadofx.*

class Invoice {
    var customerId by property<Long>()
    fun getCustomerId() = getProperty(Invoice::customerId)

    var searchCustomerName by property<String>()
    fun searchProperty() = getProperty(Invoice::searchCustomerName)

    var productsList by property<MutableList<ProductsTable>>()
    fun productsProperty() = getProperty(Invoice::productsList)

    override fun toString() = "$customerId"
}