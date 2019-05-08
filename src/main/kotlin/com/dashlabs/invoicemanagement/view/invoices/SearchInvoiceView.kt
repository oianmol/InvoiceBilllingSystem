package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.invoicemanagement.InvoiceGenerator
import com.dashlabs.invoicemanagement.State
import com.dashlabs.invoicemanagement.StateDistrict
import com.dashlabs.invoicemanagement.app.savePdf
import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.InvoiceTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.dashlabs.invoicemanagement.view.customers.Customer
import com.dashlabs.invoicemanagement.view.customers.CustomerDetailView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import tornadofx.*
import java.awt.Desktop
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import javax.json.Json

class SearchInvoiceView : View("Search Invoices") {
    private var datePicker: DatePicker? = null
    private val invoicesController: InvoicesController by inject()


    override val root = vbox {
        hbox {
            // search by date
            vbox {
                label {
                    text = "Search for invoice created by date"
                    vboxConstraints { margin = Insets(10.0) }
                }
                datepicker {
                    this@SearchInvoiceView.datePicker = this
                    vboxConstraints { margin = Insets(10.0) }
                    value = LocalDate.now()
                }

                button("Search") {
                    vboxConstraints { margin = Insets(10.0) }
                    alignment = Pos.BOTTOM_RIGHT
                    setOnMouseClicked {
                        datePicker?.value?.let {
                            val startTime = it.atTime(LocalTime.MIN)
                            val endTime = it.atTime(LocalTime.MAX)
                            invoicesController.searchInvoice(startTime, endTime)
                        }
                    }
                }
            }
        }

        vbox {
            this.add(getInvoiceView())
        }
    }

    private fun getInvoiceView(): TableView<InvoiceTable.MeaningfulInvoice> {
        return tableview<InvoiceTable.MeaningfulInvoice>(invoicesController.invoicesListObserver) {
            columnResizePolicy = SmartResize.POLICY
            stylesheets.add("jfx-table-view.css")
            vboxConstraints { margin = Insets(20.0) }
            tag = "invoices"
            column("Customer name", InvoiceTable.MeaningfulInvoice::customerName).remainingWidth()
            column("Bill Date", InvoiceTable.MeaningfulInvoice::dateCreated).remainingWidth()
            column("Amount Due", InvoiceTable.MeaningfulInvoice::outstandingAmount).remainingWidth()
            column("Bill Amount", InvoiceTable.MeaningfulInvoice::amountTotal).remainingWidth()
            onDoubleClick {
                showInvoiceDetails(invoicesController.invoicesListObserver.value[this.selectedCell!!.row])
            }
        }
    }


    private fun showInvoiceDetails(selectedItem: InvoiceTable.MeaningfulInvoice?) {
        selectedItem?.let {
            Single.fromCallable {
                val list = Gson().fromJson<ArrayList<Pair<ProductsTable, Int>>>(selectedItem.productsPurchased, object : TypeToken<ArrayList<Pair<ProductsTable, Int>>>() {}.type)
                val file = File("~/invoicedatabase", "temp.pdf")
                file.delete()
                file.createNewFile()
                InvoiceGenerator.makePDF(file, selectedItem, list.map { Pair(it.first, it.second) }.toMutableList())
                file
            }.subscribeOn(Schedulers.io()).observeOn(JavaFxScheduler.platform()).subscribe { t1, t2 ->
                t1?.let {
                    alert(Alert.AlertType.CONFIRMATION, "Invoice Information",
                            "View invoice or Save It",
                            buttons = *arrayOf(ButtonType("Save", ButtonBar.ButtonData.BACK_PREVIOUS),
                                    ButtonType("Preview", ButtonBar.ButtonData.NEXT_FORWARD),
                                    ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE)), owner = currentWindow, title = "Hey!") {
                        if (it.buttonData == ButtonBar.ButtonData.NEXT_FORWARD) {
                            try {
                                Desktop.getDesktop().browse(t1.toURI())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else if (it.buttonData == ButtonBar.ButtonData.BACK_PREVIOUS) {
                            savePdf(selectedItem, t1)
                        } else if (ButtonBar.ButtonData.CANCEL_CLOSE == it.buttonData) {

                        }
                    }
                }

            }
        }
    }

    fun requestForInvoices() {
        invoicesController.requestForInvoices()
    }
}