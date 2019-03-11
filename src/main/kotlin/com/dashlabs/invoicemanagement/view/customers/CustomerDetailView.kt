package com.dashlabs.invoicemanagement.view.customers

import com.dashlabs.invoicemanagement.InvoiceGenerator
import com.dashlabs.invoicemanagement.app.savePdf
import com.dashlabs.invoicemanagement.databaseconnection.*
import com.dashlabs.invoicemanagement.view.TransactionHistoryView
import com.dashlabs.invoicemanagement.view.invoices.InvoicesController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.layout.VBox
import tornadofx.*
import java.awt.Desktop
import java.io.File
import java.util.*

class CustomerDetailView(private val customerData: CustomersTable.MeaningfulCustomer) : View("${customerData.customerName} Details") {
    private var deductValue: Double = 0.0
    private val invoicesController: InvoicesController by inject()
    private var balanceVbox: VBox? = null

    init {
        invoicesController.getInvoicesForCustomer(customerData.customerId)
    }

    override val root = vbox {
        minWidth = 600.0

        hbox {
            vbox {
                hboxConstraints { margin = Insets(10.0) }
                button {
                    vboxConstraints { margin = Insets(10.0) }
                    text = "Transaction History"
                    setOnMouseClicked {
                        TransactionHistoryView(customerData.customerId).openWindow()
                    }
                }

                label(customerData.toString()) {
                    vboxConstraints { margin = Insets(10.0) }
                }
            }
            vbox {
                hboxConstraints { margin = Insets(10.0) }
                balanceVbox = this@vbox
            }
        }

        invoicesController.invoicesListObserver.addListener { observable, oldValue, newValue ->
            getOutstandingView()
        }

        tableview<InvoiceTable.MeaningfulInvoice>(invoicesController.invoicesListObserver) {
            columnResizePolicy = SmartResize.POLICY
            maxHeight = 300.0
            vboxConstraints { margin = Insets(20.0) }
            tag = "invoices"
            column("Bill Date", InvoiceTable.MeaningfulInvoice::dateCreated)
            column("Bill Amount", InvoiceTable.MeaningfulInvoice::amountTotal)
            column("Due Amount", InvoiceTable.MeaningfulInvoice::outstandingAmount)
            column("Received Payment", InvoiceTable.MeaningfulInvoice::paymentReceived)
            onDoubleClick {
                showInvoiceDetails(invoicesController.invoicesListObserver.value[this.selectedCell!!.row])
            }
        }

    }

    private var balanceBox: VBox? = null

    private fun getOutstandingView() {
        try {
            val outstanding = invoicesController.invoicesListObserver.map {
                it.outstandingAmount
            }.sum()

            this@CustomerDetailView.balanceBox?.let {
                it.removeFromParent()
            }

            if (outstanding > 0) {
                val balanceBox = vbox {
                    tag = "balance"
                    deductValue = outstanding

                    label {
                        text = "Total Amount due is $outstanding!"
                        vboxConstraints { margin = Insets(10.0) }
                    }

                    label {
                        text = "Please enter due amount to pay!"
                        vboxConstraints { margin = Insets(10.0) }
                    }

                    hbox {
                        textfield(outstanding.toString()) {
                            hboxConstraints { margin = Insets(10.0) }
                            this.filterInput {
                                it.controlNewText.isDouble() && it.controlNewText.toDouble() <= outstanding
                            }
                        }.textProperty().addListener { observable, oldValue, newValue ->
                            newValue.takeIf { !it.isNullOrEmpty() }?.let {
                                deductValue = newValue.toDouble()
                            } ?: kotlin.run {
                                deductValue = 0.0
                            }
                        }

                        button {
                            hboxConstraints { margin = Insets(10.0) }
                            text = "Pay Now!"
                            setOnMouseClicked {
                                if (deductValue > 0) {
                                    performBalanceReduction(customerData, deductValue)
                                }
                            }
                        }
                    }
                }
                this@CustomerDetailView.balanceBox = balanceBox
                balanceVbox?.add(balanceBox)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun showInvoiceDetails(selectedItem: InvoiceTable.MeaningfulInvoice?) {
        selectedItem?.let {
            val t1 = generateInvoice(selectedItem).blockingGet()
            alertUser(t1, selectedItem)
        }
    }

    private fun alertUser(t1: File, selectedItem: InvoiceTable.MeaningfulInvoice) {
        alert(Alert.AlertType.INFORMATION, "Invoice Information",
                "View invoice or Save It",
                buttons = *arrayOf(ButtonType("Save", ButtonBar.ButtonData.BACK_PREVIOUS),
                        ButtonType("Preview", ButtonBar.ButtonData.NEXT_FORWARD)), owner = currentWindow, title = "Hey!") {
            when {
                it.buttonData == ButtonBar.ButtonData.NEXT_FORWARD -> try {
                    Desktop.getDesktop().browse(t1.toURI())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                it.buttonData == ButtonBar.ButtonData.BACK_PREVIOUS -> savePdf(selectedItem, t1)
            }
        }
    }

    private fun generateInvoice(selectedItem: InvoiceTable.MeaningfulInvoice): Single<File> {
        return Single.fromCallable {
            val list = Gson().fromJson<ArrayList<Pair<ProductsTable, Int>>>(
                    selectedItem.productsPurchased,
                    object : TypeToken<ArrayList<Pair<ProductsTable, Int>>>() {}.type)
            val file = File("~/invoicedatabase", "temp.pdf")
            file.delete()
            file.createNewFile()
            InvoiceGenerator.makePDF(file, selectedItem, list.map { Pair(it.first, it.second) }.toMutableList())
            file
        }.subscribeOn(Schedulers.io()).observeOn(JavaFxScheduler.platform())
    }


    private fun performBalanceReduction(selectedItem: CustomersTable.MeaningfulCustomer, deductValue: Double) {
        Single.fromCallable {
            val customer = Database.getCustomer(selectedItem.customerId)
            customer?.let {
                Database.updateCustomer(customer, deductValue)

                val transactionTable = TransactionTable()
                transactionTable.dateCreated = System.currentTimeMillis()
                transactionTable.customerId = selectedItem.customerId
                transactionTable.deduction = deductValue
                Database.createTransaction(transactionTable)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform()).subscribe { t1, t2 ->
                    t1?.let {
                        find<CustomersView> {
                            requestForCustomers()
                        }
                    }
                    t2?.let {
                        it.message?.let { it1 -> warning(it1).show() }
                    }
                    invoicesController.getInvoicesForCustomer(selectedItem.customerId)
                }
    }
}
