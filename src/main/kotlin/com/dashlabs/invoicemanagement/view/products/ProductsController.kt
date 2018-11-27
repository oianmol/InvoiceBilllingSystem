package com.dashlabs.invoicemanagement.view.products

import com.dashlabs.invoicemanagement.databaseconnection.Database
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import tornadofx.*

class ProductsController : Controller() {

    val productsListObserver  = SimpleListProperty<ProductsTable>()

    fun requestForProducts() {
        val listOfProducts = Database.listProducts()
        runLater {
            listOfProducts?.let {
                productsListObserver.set(FXCollections.observableArrayList(it))
            }
        }
    }

}