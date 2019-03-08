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
import javafx.scene.layout.VBox
import tornadofx.*
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import java.util.*

class CustomerDetailView(private val customerData: CustomersTable) : View("${customerData.customerName} Details") {
    private var deductValue: Double = 0.0
    private val invoicesController: InvoicesController by inject()
    private var balanceVbox: VBox? = null

    init {
        invoicesController.getInvoicesForCustomer(customerData.customerId)
    }

    override val root = vbox {
        minHeight = 800.0
        minWidth = 600.0
        label(customerData.toString()) {
            vboxConstraints { margin = Insets(10.0) }
        }

        vbox {
            vboxConstraints { margin = Insets(10.0) }
            balanceVbox = this@vbox
        }

        invoicesController.invoicesListObserver.addListener { observable, oldValue, newValue ->
            getOutstandingView()
        }

        tableview<InvoiceTable>(invoicesController.invoicesListObserver) {
            columnResizePolicy = SmartResize.POLICY
            maxHeight = 300.0
            vboxConstraints { margin = Insets(20.0) }
            tag = "invoices"
            column("Date Modified", InvoiceTable::dateModified)
            column("Outstanding Amount", InvoiceTable::outstandingAmount)
            column("Amount Total", InvoiceTable::amountTotal)
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
                    button {
                        vboxConstraints { margin = Insets(10.0) }
                        text = "Pay full pending amount! ${outstanding}"
                        setOnMouseClicked {
                            deductValue = outstanding
                            performBalanceReduction(customerData, outstanding)
                        }
                    }

                    label {
                        text = "Or pay some amount partial amount "
                        vboxConstraints { margin = Insets(10.0) }
                    }
                    deductValue = outstanding

                    hbox {
                        textfield(outstanding.toString()) {
                            hboxConstraints { margin = Insets(10.0) }
                            this.filterInput {
                                it.controlNewText.isDouble() && it.controlNewText.toDouble() <= outstanding
                            }
                        }.textProperty().addListener { observable, oldValue, newValue ->
                            deductValue = newValue.toDouble()
                        }

                        button {
                            hboxConstraints { margin = Insets(10.0) }
                            text = "Pay some pending amount from ${outstanding}"
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

    /**
     * Convert from a filename to a file URL.
     */
    private fun convertToFileURL(filename: String): String {
        // On JDK 1.2 and later, simplify this to:
        // "path = file.toURL().toString()".
        var path = File(filename).absolutePath
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/')
        }
        if (!path.startsWith("/")) {
            path = "/$path"
        }

        return "file:$path"
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
                                Desktop.getDesktop().browse(URL(convertToFileURL(t1.absolutePath)).toURI())
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


    private fun performBalanceReduction(selectedItem: CustomersTable, deductValue: Double) {
        Single.fromCallable {
            val customer = Database.getCustomer(selectedItem.customerId)
            customer?.let {
                Database.updateCustomer(customer, deductValue)
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
