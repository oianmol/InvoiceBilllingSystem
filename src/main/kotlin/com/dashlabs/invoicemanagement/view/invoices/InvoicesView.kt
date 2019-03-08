package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.WebViewFactory
import com.dashlabs.invoicemanagement.InvoiceGenerator
import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.InvoiceTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.dashlabs.invoicemanagement.view.customers.CustomersView
import com.dashlabs.invoicemanagement.view.customers.OnCustomerSelectedListener
import com.dashlabs.invoicemanagement.view.customers.OnProductSelectedListener
import com.dashlabs.invoicemanagement.view.products.ProductsView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.collections.FXCollections
import javafx.collections.ObservableMap
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.DatePicker
import javafx.scene.layout.VBox
import tornadofx.*
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.util.ArrayList
import com.sun.javaws.BrowserSupport.showDocument
import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory
import java.net.URISyntaxException
import java.io.IOException
import sun.net.www.ParseUtil.toURI
import java.awt.Desktop
import java.net.URL


class InvoicesView : View("Invoices View") {

    private val invoiceViewModel = InvoiceViewModel()
    private val invoicesController: InvoicesController by inject()
    private var productsView: VBox? = null

    override val root = scrollpane {
        content = hbox {
            this.add(createInvoiceView())

            this.add(oldInvoicesView())
        }
    }

    private var datePicker: DatePicker? = null

    private fun oldInvoicesView(): VBox {
        return vbox {

            hbox {

                label {
                    text = "Search for invoice created by date"
                    hboxConstraints { margin = Insets(10.0) }
                }
                datepicker {
                    this@InvoicesView.datePicker = this
                    hboxConstraints { margin = Insets(10.0) }
                    value = LocalDate.now()
                }

                button("Search") {
                    hboxConstraints { margin = Insets(10.0) }
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

            tableview<InvoiceTable>(invoicesController.invoicesListObserver) {
                columnResizePolicy = SmartResize.POLICY
                maxHeight = 300.0
                vboxConstraints { margin = Insets(20.0) }
                tag = "invoices"
                column("Invoice Id", InvoiceTable::invoiceId)
                column("Date Modified", InvoiceTable::dateModified)
                column("Customer Id", InvoiceTable::customerId)
                onDoubleClick {
                    showInvoiceDetails(invoicesController.invoicesListObserver.value[this.selectedCell!!.row])
                }
            }
        }
    }


    private fun createInvoiceView(): VBox {
        return vbox {
            this@InvoicesView.productsView = this
            label(invoiceViewModel.customer) {
                vboxConstraints { margin = Insets(10.0, 0.0, 10.0, 10.0) }
            }

            this.add(produtCustomerButtonView())

            this.add(getSelectedProductsView())

            vbox {
                vboxConstraints { margin = Insets(0.0, 0.0, 0.0, 10.0) }
                label("Total Amount: ") {
                    vboxConstraints { margin = Insets(10.0) }
                }

                textfield(invoiceViewModel.totalPrice) {
                    this.isEditable = false
                    this.filterInput { it.controlNewText.isDouble() }
                }
            }

            vbox {
                vboxConstraints { margin = Insets(0.0, 0.0, 0.0, 10.0) }

                label("Credit Amount: ") {
                    vboxConstraints { margin = Insets(10.0) }
                }

                textfield(invoiceViewModel.creditAmount) {
                    this.filterInput {
                        it.controlNewText.isDouble() && it.controlNewText.toDouble() <= invoiceViewModel.totalPrice.value.toDouble()
                    }
                }.textProperty().addListener { observable, oldValue, newValue ->
                    try {
                        newValue.takeIf { !it.isNullOrEmpty() }?.let {
                            val bal = invoiceViewModel.totalPrice.value.toDouble().minus(it.toDouble()).toString()
                            invoiceViewModel.payableAmount.value = bal
                        }?:kotlin.run {
                            val bal = invoiceViewModel.totalPrice.value.toDouble().toString()
                            invoiceViewModel.payableAmount.value = bal
                        }
                    } catch (ex: Exception) {

                    }
                }

            }

            vbox {
                vboxConstraints { margin = Insets(0.0, 0.0, 0.0, 10.0) }

                label("Payable Amount: ") {
                    vboxConstraints { margin = Insets(10.0) }
                }

                textfield(invoiceViewModel.payableAmount) {
                    this.isEditable = false
                }

            }

            button("Create Invoice") {
                vboxConstraints { margin = Insets(10.0) }
                setOnMouseClicked {
                    if (invoiceViewModel.customerId.value == 0L || invoicesController.productsListObserver.value.isEmpty()) {
                        warning("Select products and customer first!").show()
                        return@setOnMouseClicked
                    } else {
                        if (invoiceViewModel.creditAmount.value != null && invoiceViewModel.creditAmount.value.toDouble() > 0) {
                            alert(Alert.AlertType.CONFIRMATION, "Credit Amount",
                                    "You are not paying in full and amount ${invoiceViewModel.creditAmount.value} will be added to your credits",
                                    buttons = *arrayOf(ButtonType.OK, ButtonType.CANCEL), owner = currentWindow, title = "Hey!") {
                                if (it == ButtonType.OK) {
                                    invoicesController.addInvoice(invoiceViewModel)
                                }
                            }
                        } else {
                            invoicesController.addInvoice(invoiceViewModel)
                        }
                    }
                }
            }
        }
    }

    private fun produtCustomerButtonView(): VBox {
        return vbox {
            hbox {
                button("Select Products") {
                    hboxConstraints { margin = Insets(0.0, 10.0, 0.0, 10.0) }
                    setOnMouseClicked {
                        ProductsView(onProductSelectedListener = object : OnProductSelectedListener {
                            override fun onProductSelected(newSelectedProducts: ObservableMap<ProductsTable, Int>?) {
                                val currentList = FXCollections.observableHashMap<ProductsTable, Int>()
                                invoicesController.productsListObserver.value?.let {
                                    currentList.putAll(it)
                                }
                                newSelectedProducts?.forEach { item ->
                                    currentList[item.key]?.let {
                                        currentList[item.key] = item.value + 1
                                    } ?: kotlin.run {
                                        currentList[item.key] = 1
                                    }
                                }
                                invoiceViewModel.totalPrice.value = currentList.map { it.key.amount * it.value }?.sum().toString()
                                invoiceViewModel.payableAmount.value = currentList.map { it.key.amount * it.value }?.sum().toString()
                                invoiceViewModel.productsList.value = currentList
                                invoicesController.updateProductsObserver(currentList)
                            }
                        }).openWindow()
                    }
                }

                button("Select Customer") {
                    hboxConstraints { margin = Insets(0.0, 10.0, 0.0, 10.0) }
                    setOnMouseClicked {
                        CustomersView(onCustomerSelectedListener = object : OnCustomerSelectedListener {
                            override fun onCustomerSelected(customersTable: CustomersTable) {
                                invoiceViewModel.customerId.value = customersTable.customerId
                                invoiceViewModel.customer.value = customersTable
                            }

                        }).openWindow()
                    }
                }

            }

        }
    }

    private fun getSelectedProductsView(): VBox {
        return vbox {
            vboxConstraints { margin = Insets(10.0) }
            invoicesController.productsListObserver.onChange {

            }
            scrollpane {
                vboxConstraints { margin = Insets(20.0) }
                label(invoicesController.productslist) {
                    style {
                        fontSize = Dimension(15.0, Dimension.LinearUnits.px)
                    }
                }
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

                        try {
                            Desktop.getDesktop().browse(URL("file://${t1.absolutePath}").toURI())
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } catch (e: URISyntaxException) {
                            e.printStackTrace()
                        }

                    }
                    //InvoiceDetailView(it, customer).openWindow()
                }
            }
        }
    }

    fun requestForInvoices() {
        invoicesController.requestForInvoices()
    }
}