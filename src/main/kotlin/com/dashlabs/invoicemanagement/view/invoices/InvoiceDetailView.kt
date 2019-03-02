package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.InvoiceTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.stage.Screen
import tornadofx.*
import java.util.*


class InvoiceDetailView(selectedItem: InvoiceTable, customer: CustomersTable) : View("Invoice Details") {
    override val root = vbox {

        selectedItem.customerId
        selectedItem.dateCreated
        selectedItem.dateModified
        selectedItem.invoiceId
        selectedItem.productsPurchased

        val list = Gson().fromJson<MutableList<ProductsTable>>(selectedItem.productsPurchased, object : TypeToken<List<ProductsTable>>() {}.type)
        val obsProducts = FXCollections.observableArrayList(list)

        label(text = customer.toString()) {
            vboxConstraints { margin = Insets(10.0) }
        }

        label(text = "Last Modified on ${Date(selectedItem.dateModified)}") {
            vboxConstraints { margin = Insets(10.0) }
        }

        label(text = "Total Amount: ${list.map { it.amount }.sum()}") {
            vboxConstraints { margin = Insets(10.0) }
        }

        tableview(obsProducts) {
            this.minWidth = Screen.getPrimary().visualBounds.width / 3
            tag = "products"
            vboxConstraints { margin = Insets(10.0) }
            column("ID", ProductsTable::productId)
            column("Product Name", ProductsTable::productName)
            column("Amount", ProductsTable::amount)
        }

    }
}
