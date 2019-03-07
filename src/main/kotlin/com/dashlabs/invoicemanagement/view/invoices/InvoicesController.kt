package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.invoicemanagement.InvoiceGenerator
import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.Database
import com.dashlabs.invoicemanagement.databaseconnection.InvoiceTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.dashlabs.invoicemanagement.view.customers.CustomersView
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableMap
import javafx.stage.FileChooser
import org.sqlite.util.StringUtils
import tornadofx.*
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime

class InvoicesController : Controller() {

    val invoicesListObserver = SimpleListProperty<InvoiceTable>()
    val productsListObserver = SimpleMapProperty<ProductsTable, Int>()
    var productslist = SimpleStringProperty()

    fun requestForInvoices() {
        val listOfInvoices = Database.listInvoices()
        runLater {
            listOfInvoices?.let {
                invoicesListObserver.set(FXCollections.observableArrayList(it))
            }
        }
    }


    fun updateProductsObserver(productsTable: ObservableMap<ProductsTable, Int>?) {
        runLater {
            productsTable?.let {
                this.productsListObserver.set(productsTable)
                val listedProducts = productsListObserver?.value?.map { it.key.productName + " (${it.key.amount} x " + it.value + ") = ${it.key.amount.times(it.value)}" }
                productslist.value = StringUtils.join(listedProducts, "\n\n")
            } ?: kotlin.run {
                this.productsListObserver.set(FXCollections.observableHashMap())
            }
        }
    }

    fun searchInvoice(startTime: LocalDateTime, endTime: LocalDateTime) {
        Single.create<List<InvoiceTable>> {
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
                        invoicesListObserver.set(FXCollections.observableArrayList<InvoiceTable>(it))
                    }
                }
    }

    fun addInvoice(invoiceViewModel: InvoiceViewModel): Single<InvoiceTable> {
        val subscription = Single.create<InvoiceTable> {
            try {
                val invoice = Invoice()
                invoice.customerId = invoiceViewModel.customerId.value
                invoice.productsList = invoiceViewModel.productsList.value
                invoice.creditAmount = invoiceViewModel.creditAmount.value
                invoice.productsPrice = invoiceViewModel.totalPrice.value
                var products = hashMapOf<ProductsTable, Int>()
                productsListObserver.value.forEach {
                    products[it.key] = it.value
                }
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
                    val listproducts = productsListObserver?.value?.map { Pair(it.key, it.value) }?.toMutableList()
                    val file = File("~/invoicedatabase","tempinv.pdf")
                    InvoiceGenerator.makePDF(file, it, listproducts!!)
                    file
                }.subscribeOn(Schedulers.io()).observeOn(JavaFxScheduler.platform()).subscribe { file, t2 ->
                    updateProductsObserver(null)
                    productslist.value = null
                    val fileChooser = FileChooser()
                    fileChooser.initialFileName = "${it.customerId}-${it.invoiceId}-${it.dateModified}.pdf"
                    val extFilter = FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf")
                    fileChooser.extensionFilters.add(extFilter)
                    fileChooser.title = "Save Invoice"
                    val dest = fileChooser.showSaveDialog(null)
                    if (dest != null) {
                        try {
                            Files.copy(file.toPath(), dest.toPath())
                        } catch (ex: Exception) {

                        }

                    }
                    find<CustomersView> {
                        requestForCustomers()
                    }
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

}

class InvoiceViewModel : ItemViewModel<Invoice>(Invoice()) {
    fun clearValues() {
        customerId.value = null
        productsList.value = null
        customer.value = null
        creditAmount.value = null
        totalPrice.value = null
        payableAmount.value = null
    }

    val customerId = bind(Invoice::customerId)
    val productsList = bind(Invoice::productsList)
    var customer = bind(Invoice::customer)
    var totalPrice = bind(Invoice::productsPrice)
    var creditAmount = bind(Invoice::creditAmount)
    var payableAmount = bind(Invoice::payableAmount)
    var searchCustomer = bind(Invoice::searchCustomer)
}