package com.dashlabs.invoicemanagement.view.customers

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import javafx.collections.ObservableMap

interface OnCustomerSelectedListener {
    fun onCustomerSelected(customersTable: CustomersTable)
}

interface OnProductSelectedListener {
    fun onProductSelected(newSelectedProducts: ObservableMap<ProductsTable, Int>?)
}