package com.dashlabs.invoicemanagement.view.products

import com.dashlabs.invoicemanagement.databaseconnection.Database
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.Property
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

    fun searchProduct(username: Property<String>) {
        Single.create<List<ProductsTable>> {
            try {
                val listOfProducts = Database.listProducts(search = username.value)
                listOfProducts?.let { it1 -> it.onSuccess(it1) }
            } catch (ex: Exception) {
                it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe { t1, t2 ->
                    t1?.let {
                        it.isNotEmpty().let {
                            if (it) {
                                username.value = ""
                            }
                        }
                        productsListObserver.set(FXCollections.observableArrayList<ProductsTable>(it))
                    }
                }
    }

    fun addProduct(productName: Property<String>, amountName: Property<Number>) {
        Single.create<ProductsTable> {
            try {
                if (productName.value.isNullOrEmpty() || amountName.value.toDouble() == 0.0) {
                    it.onError(Exception())
                } else {
                    val product = Product()
                    product.name = productName.value
                    product.amount = amountName.value.toDouble()
                    Database.createProduct(product)?.let { it1 -> it.onSuccess(it1) }
                }
            } catch (ex: Exception) {
                it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe { t1, t2 ->
                    t1?.let {
                        productName.value = ""
                        amountName.value = 0.0
                        productsListObserver.add(it)
                    }
                }
    }

}

class ProductViewModel : ItemViewModel<Product>(Product()) {

    val productName = bind(Product::nameProperty)
    val amountName = bind(Product::amountProperty)
    val searchName = bind(Product::searchProperty)

}