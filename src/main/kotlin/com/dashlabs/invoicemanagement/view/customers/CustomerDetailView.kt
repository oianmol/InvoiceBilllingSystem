package com.dashlabs.invoicemanagement.view.customers

import com.dashlabs.invoicemanagement.InvoiceGenerator
import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.Database
import com.dashlabs.invoicemanagement.databaseconnection.InvoiceTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.dashlabs.invoicemanagement.view.invoices.InvoicesController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.geometry.Insets
import tornadofx.*
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import java.util.*

class CustomerDetailView(selectedItem: CustomersTable) : View("${selectedItem.customerName} Details") {
    private var deductValue: Double = 0.0
    private val invoicesController: InvoicesController by inject()

    init {
        invoicesController.getInvoicesForCustomer(selectedItem.customerId)
    }

    override val root = vbox {
        label(selectedItem.toString()) {
            vboxConstraints { margin = Insets(10.0) }
        }

        /*if (selectedItem.balance > 0) {
            button {
                vboxConstraints { margin = Insets(10.0) }
                text = "Pay full pending amount! ${selectedItem.balance}"
                setOnMouseClicked {
                    deductValue = selectedItem.balance
                    performBalanceReduction(selectedItem)
                }
            }

            label { text = "Or ->>" }

            hbox {
                textfield(selectedItem.balance.toString()) {
                    hboxConstraints { margin = Insets(10.0) }
                    this.filterInput {
                        it.controlNewText.isDouble() && it.controlNewText.toDouble() <= selectedItem.balance
                    }
                }.textProperty().addListener { observable, oldValue, newValue ->
                    deductValue = newValue.toDouble()
                }

                button {
                    hboxConstraints { margin = Insets(10.0) }
                    text = "Pay some pending amount from ${selectedItem.balance}"
                    setOnMouseClicked {
                        if (deductValue > 0) {
                            performBalanceReduction(selectedItem)
                        }
                    }
                }
            }
        }*/

        tableview<InvoiceTable>(invoicesController.invoicesListObserver) {
            columnResizePolicy = SmartResize.POLICY
            maxHeight = 300.0
            vboxConstraints { margin = Insets(20.0) }
            tag = "invoices"
            column("Invoice Id", InvoiceTable::invoiceId)
            column("Date Modified", InvoiceTable::dateModified)
            column("Customer Id", InvoiceTable::customerId)
            column("Amount Paid",InvoiceTable::amountPaid)
            column("Amount Total",InvoiceTable::amountTotal)
            onDoubleClick {
                showInvoiceDetails(invoicesController.invoicesListObserver.value[this.selectedCell!!.row])
            }
        }

    }

    private fun showInvoiceDetails(selectedItem: InvoiceTable?) {
        selectedItem?.let {
            invoicesController.getCustomerById(selectedItem.customerId).subscribe { t1, t2 ->
                t1?.let { customer ->
                    Single.fromCallable {
                        val list = Gson().fromJson<ArrayList<Pair<ProductsTable, Int>>>(selectedItem.productsPurchased, object : TypeToken<ArrayList<Pair<ProductsTable, Int>>>() {}.type)
                        val file = File("~/invoicedatabase", "temp.pdf")
                        file.delete()
                        file.createNewFile()
                        InvoiceGenerator.makePDF(file, selectedItem, list.map { Pair(it.first, it.second) }.toMutableList())
                        file
                    }.subscribeOn(Schedulers.io()).observeOn(JavaFxScheduler.platform()).subscribe { t1, t2 ->
                        //val hostServices = HostServicesFactory.getInstance(this@InvoicesView.app)
                        //hostServices.showDocument("file://$t1.absolutePath")
                        t1?.let {
                            try {
                                Desktop.getDesktop().browse(URL("file://${t1.absolutePath}").toURI())
                            } catch (e: IOException) {
                                e.printStackTrace()
                            } catch (e: URISyntaxException) {
                                e.printStackTrace()
                            }
                        }

                    }
                    //InvoiceDetailView(it, customer).openWindow()
                }
            }
        }
    }


    private fun performBalanceReduction(selectedItem: CustomersTable) {
        Single.fromCallable {
            val customer = Database.getCustomer(selectedItem.customerId)
            customer?.let {
                //it.balance = it.balance.minus(deductValue)
                Database.updateCustomer(customer)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform()).subscribe { t1, t2 ->
                    t1?.let {
                        find<CustomersView> {
                            requestForCustomers()
                            this@CustomerDetailView.close()
                        }
                    }
                    t2?.let {
                        it.message?.let { it1 -> warning(it1).show() }
                    }

                }
    }
}
