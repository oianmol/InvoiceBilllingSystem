package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.invoicemanagement.databaseconnection.Database
import com.dashlabs.invoicemanagement.databaseconnection.InvoiceTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.Property
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*

class InvoicesController : Controller() {

    val invoicesListObserver = SimpleListProperty<InvoiceTable>()
    val productsListObserver = SimpleListProperty<ProductsTable>()

    fun requestForInvoices() {
        val listOfInvoices = Database.listInvoices()
        runLater {
            listOfInvoices?.let {
                invoicesListObserver.set(FXCollections.observableArrayList(it))
            }
        }
    }

    fun updateProductsObserver(productsTable: ObservableList<ProductsTable>?) {
        runLater {
            productsTable?.let {
                this.productsListObserver.set(productsTable)
            } ?: kotlin.run {
                this.productsListObserver.set(FXCollections.observableArrayList(listOf()))
            }
        }
    }

    fun searchInvoice(customerName: Property<String>) {
        Single.create<List<InvoiceTable>> {
            try {
                val listOfInvoices = Database.listInvoices(customerId = customerName.value)
                listOfInvoices?.let { it1 -> it.onSuccess(it1) }
            } catch (ex: Exception) {
                it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe { t1, t2 ->
                    t1?.let {
                        it.isNotEmpty().let {
                            if (it) {
                                customerName.value = ""
                            }
                        }
                        invoicesListObserver.set(FXCollections.observableArrayList<InvoiceTable>(it))
                    }
                }
    }

    fun addInvoice(invoiceViewModel: InvoiceViewModel): Single<InvoiceTable> {
        val subscription = Single.create<InvoiceTable> {
            try {
                val customerId = invoiceViewModel.customerId
                val productsList = invoiceViewModel.productsList
                val invoice = Invoice()
                invoice.customerId = customerId.value
                invoice.productsList = productsList.value
                Database.createInvoice(invoice)?.let { it1 -> it.onSuccess(it1) }
            } catch (ex: Exception) {
                ex.printStackTrace()
                it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())

        subscription.subscribe { t1, t2 ->
            t1?.let {
                invoicesListObserver.add(it)
                print(it)
                invoiceViewModel.clearValues()
                updateProductsObserver(null)
            }
            t2?.let {
                print(it)
            }
        }

        return subscription

    }

}

class InvoiceViewModel : ItemViewModel<Invoice>(Invoice()) {

    fun clearValues() {
        customerId.value = null
        productsList.value = null
        customer.value = null
    }

    val customerId = bind(Invoice::customerId)
    val productsList = bind(Invoice::productsList)
    var customer = bind(Invoice::customer)
}