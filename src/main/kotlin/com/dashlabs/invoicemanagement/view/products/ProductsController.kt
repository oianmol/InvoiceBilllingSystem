package com.dashlabs.invoicemanagement.view.products

import com.dashlabs.invoicemanagement.databaseconnection.Database
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import tornadofx.*

class ProductsController : Controller() {

    val productsListObserver = SimpleListProperty<ProductsTable>()
    fun requestForProducts() {
        val listOfProducts = Database.listProducts()
        runLater {
            listOfProducts?.let {
                productsListObserver.set(FXCollections.observableArrayList(it))
            }
        }
    }

    fun searchFor(s: String) {
        Single.create<List<ProductsTable>> {
            try {
                val listOfProducts = Database.listProducts(search = s)
                listOfProducts?.let { it1 -> it.onSuccess(it1) }
            } catch (ex: Exception) {
                it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe { t1, t2 ->
                    t1?.let {
                        productsListObserver.set(FXCollections.observableArrayList<ProductsTable>(it))
                    }
                }
    }

}