package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import tornadofx.*

class Invoice {
    var customerId by property<Long>()

    var customer by property<CustomersTable>()

    var productsList by property<ObservableList<InvoicesController.ProductsModel>>()

    var productsPrice by property<Number>()

    var creditAmount by property<Number>()

    var payableAmount by property<Number>()

    override fun toString() = "$customerId"
}