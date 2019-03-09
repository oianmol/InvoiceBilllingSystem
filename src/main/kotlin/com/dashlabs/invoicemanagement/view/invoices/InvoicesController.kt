package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.invoicemanagement.InvoiceGenerator
import com.dashlabs.invoicemanagement.app.savePdf
import com.dashlabs.invoicemanagement.databaseconnection.*
import com.dashlabs.invoicemanagement.view.customers.CustomersView
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import java.io.File
import java.time.LocalDateTime

class InvoicesController : Controller() {

    class ProductsModel(var productsTable: ProductsTable, var quantity: String, var totalAmount: String, var baseAmount: Double)


    val invoicesListObserver = SimpleListProperty<InvoiceTable.MeaningfulInvoice>()
    val customersListObservable = SimpleListProperty<CustomersTable>()

    val transactionListObserver = SimpleListProperty<TransactionTable.MeaningfulTransaction>()

    val productsQuanityView = FXCollections.observableArrayList<ProductsModel>()

    fun requestForInvoices() {
        val listOfInvoices = Database.listInvoices()
        runLater {
            listOfInvoices?.let {
                invoicesListObserver.set(FXCollections.observableArrayList(it))
            }
        }
    }


    fun updateProductsObserver(productsTable: ObservableList<ProductsModel>?) {
        runLater {
            productsTable?.let {
                productsQuanityView.setAll(productsTable)
            } ?: kotlin.run {
                this.productsQuanityView.setAll(FXCollections.observableList(listOf()))
            }
        }
    }

    fun searchCustomers(state: String, district: String) {
        Single.create<List<CustomersTable>> {
            try {
                val listOfInvoices = Database.listCustomers(state, district)
                listOfInvoices?.let { it1 -> it.onSuccess(it1) }
            } catch (ex: Exception) {
                it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe { t1, t2 ->
                    t1?.let {
                        customersListObservable.set(FXCollections.observableArrayList<CustomersTable>(it))
                    }
                }
    }

    fun searchInvoice(startTime: LocalDateTime, endTime: LocalDateTime) {
        Single.create<List<InvoiceTable.MeaningfulInvoice>> {
            try {
                val listOfInvoices = Database.listInvoices(startTime, endTime)
                listOfInvoices?.let { it1 -> it.onSuccess(it1) }
            } catch (ex: Exception) {
                it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe { t1, t2 ->
                    t1?.let {
                        invoicesListObserver.set(FXCollections.observableArrayList<InvoiceTable.MeaningfulInvoice>(it))
                    }
                }
    }

    fun addInvoice(invoiceViewModel: InvoiceViewModel): Single<InvoiceTable.MeaningfulInvoice> {
        val subscription = Single.create<InvoiceTable.MeaningfulInvoice> {
            try {
                val invoice = Invoice()
                invoice.customerId = invoiceViewModel.customerId.value
                invoice.productsList = invoiceViewModel.productsList.value
                invoice.creditAmount = invoiceViewModel.leftoverAmount.value
                invoice.productsPrice = invoiceViewModel.totalPrice.value
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
                Single.fromCallable {
                    val listproducts = productsQuanityView?.map { Pair(it.productsTable, it.quantity.toInt()) }?.toMutableList()
                    val file = File("~/invoicedatabase", "tempinv.pdf")
                    InvoiceGenerator.makePDF(file, it, listproducts!!)
                    file
                }.subscribeOn(Schedulers.io()).observeOn(JavaFxScheduler.platform()).subscribe { file, t2 ->
                    updateProductsObserver(null)

                    find<CustomersView> {
                        requestForCustomers()
                    }

                    savePdf(it, file)
                }
            }
            t2?.let {
                print(it)
            }
        }

        return subscription

    }

    fun getCustomerById(customerId: Long): Single<CustomersTable?> {
        return Single.fromCallable {
            Database.getCustomer(customerId)
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
    }

    fun getInvoicesForCustomer(customerId: Long) {
        val listOfInvoices = Database.listInvoices(customerId)
        runLater {
            listOfInvoices?.let {
                invoicesListObserver.set(FXCollections.observableArrayList(it))
            }
        }
    }

    fun getTransactionHistory(customerId: Long) {
        val listOfInvoices = Database.listTransactions(customerId)
        runLater {
            listOfInvoices?.let {
                transactionListObserver.set(FXCollections.observableArrayList(it))
            }
        }
    }

}

class InvoiceViewModel : ItemViewModel<Invoice>(Invoice()) {
    fun clearValues() {
        customerId.value = null
        productsList.value = null
        customer.value = null
        payingAmount.value = null
        totalPrice.value = null
        leftoverAmount.value = null
    }

    val customerId = bind(Invoice::customerId)
    val productsList = bind(Invoice::productsList)
    var customer = bind(Invoice::customer)
    var totalPrice = bind(Invoice::productsPrice)
    var payingAmount = bind(Invoice::creditAmount)
    var leftoverAmount = bind(Invoice::payableAmount)
}