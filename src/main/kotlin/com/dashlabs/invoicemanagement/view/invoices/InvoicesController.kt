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
import tornadofx.*

class InvoicesController : Controller() {

    private val invoicesListObserver = SimpleListProperty<InvoiceTable>()

    fun requestForInvoices() {
        val listOfInvoices = Database.listInvoices()
        runLater {
            listOfInvoices?.let {
                invoicesListObserver.set(FXCollections.observableArrayList(it))
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

    fun addInvoice(customerId: Property<Long>,
                   productsList: Property<MutableList<ProductsTable>>) {
        Single.create<InvoiceTable> {
            try {
                if (customerId.value.toString().isEmpty() || productsList.value.isEmpty()) {
                    it.onError(Exception())
                } else {
                    val invoice = Invoice()
                    invoice.customerId = customerId.value
                    invoice.productsList = productsList.value
                    Database.createInvoice(invoice)?.let { it1 -> it.onSuccess(it1) }
                }
            } catch (ex: Exception) {
                it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe { t1, t2 ->
                    t1?.let {
                        invoicesListObserver.add(it)
                        print(it)
                    }
                    t2?.let {
                        print(it)
                    }
                }
    }

}

class InvoiceViewModel : ItemViewModel<Invoice>(Invoice()) {
    val customerId = bind(Invoice::customerId)
    val productsList = bind(Invoice::productsList)
    val searchCustomerName = bind(Invoice::searchCustomerName)
}