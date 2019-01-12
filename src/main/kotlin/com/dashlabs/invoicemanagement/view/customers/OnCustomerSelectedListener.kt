package com.dashlabs.invoicemanagement.view.customers

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable

interface OnCustomerSelectedListener {
    fun onCustomerSelected(customersTable: CustomersTable)
}

interface OnProductSelectedListener {
    fun onProductSelected(productsTable: MutableList<ProductsTable>?)
}