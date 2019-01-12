package com.dashlabs.invoicemanagement.view.customers

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import javafx.collections.ObservableList

interface OnCustomerSelectedListener {
    fun onCustomerSelected(customersTable: CustomersTable)
}

interface OnProductSelectedListener {
    fun onProductSelected(productsTable: ObservableList<ProductsTable>?)
}