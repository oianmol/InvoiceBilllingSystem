package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import tornadofx.*

class Invoice {
    var customerId by property<Long>()
    fun getCustomerId() = getProperty(Invoice::customerId)

    var customer by property<CustomersTable>()
    fun getCustomer() =  getProperty(Invoice::customer)

    var productsList by property<MutableList<ProductsTable>>()
    fun productsProperty() = getProperty(Invoice::productsList)

    var productsPrice by property<String>()
    fun productsPriceProperty() = getProperty(Invoice::productsPrice)

    var creditAmount by property<Number>()
    fun creditAmountProperty() = getProperty(Invoice::creditAmount)

    var payableAmount by property<String>()
    fun payableAmountProp() = getProperty(Invoice::payableAmount)

    override fun toString() = "$customerId"
}