package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.InvoiceTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.dashlabs.invoicemanagement.view.customers.CustomersView
import com.dashlabs.invoicemanagement.view.customers.OnCustomerSelectedListener
import com.dashlabs.invoicemanagement.view.customers.OnProductSelectedListener
import com.dashlabs.invoicemanagement.view.products.ProductsView
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.layout.VBox
import javafx.stage.Screen
import tornadofx.*

class InvoicesView : View("Invoices View") {

    private val invoiceViewModel = InvoiceViewModel()
    private val invoicesController: InvoicesController by inject()
    private var productsView: VBox? = null

    override val root = hbox {

        label(invoiceViewModel.customer)

        this.add(getProductsView())

        this.add(oldInvoicesView())
    }

    private fun oldInvoicesView(): VBox {
        return vbox {
            tableview<InvoiceTable>(invoicesController.invoicesListObserver) {
                this.minWidth = Screen.getPrimary().visualBounds.width / 2
                vboxConstraints { margin = Insets(10.0) }
                tag = "invoices"
                column("Invoice Id", InvoiceTable::invoiceId)
                column("Date Modified", InvoiceTable::dateModified)
                column("Customer Id", InvoiceTable::customerId)
                column("Products Purchased", InvoiceTable::productsPurchased)
                onDoubleClick {
                    showInvoiceDetails(this.selectedItem)
                }
            }
        }
    }


    private fun getProductsView(): VBox {
        return vbox {
            this@InvoicesView.productsView = this
            vboxConstraints { margin = Insets(10.0) }

            this.add(produtCustomerButtonView())

            this.add(getSelectedProductsView())

            vbox {
                vbox {
                    vboxConstraints { margin = Insets(10.0) }
                    label("Total Amount: ") {
                        vboxConstraints { margin = Insets(10.0) }
                    }

                    textfield(invoiceViewModel.totalPrice) {
                        this.isEditable = false
                        this.filterInput { it.controlNewText.isDouble() }
                    }
                }

                vbox {
                    vboxConstraints { margin = Insets(10.0) }
                    label("Credit Amount: ") {
                        vboxConstraints { margin = Insets(10.0) }
                    }

                    textfield(invoiceViewModel.creditAmount) {
                        this.filterInput {
                            it.controlNewText.isDouble() && it.controlNewText.toDouble() <= invoiceViewModel.totalPrice.value.toDouble()
                        }
                    }.textProperty().addListener { observable, oldValue, newValue ->
                        try {
                            val bal = invoiceViewModel.totalPrice.value.toDouble().minus(invoiceViewModel.creditAmount.value.toDouble()).toString()
                            invoiceViewModel.payableAmount.value = bal
                        } catch (ex: Exception) {

                        }
                    }

                }

                vbox {
                    vboxConstraints { margin = Insets(10.0) }
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
                        if (invoiceViewModel.customerId.value == 0L || invoiceViewModel.productsList.value.isEmpty()) {
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
    }

    private fun produtCustomerButtonView(): VBox {
        return vbox {
            vboxConstraints { margin = Insets(10.0) }

            hbox {
                button("Select Products") {
                    hboxConstraints { margin = Insets(0.0, 10.0, 0.0, 10.0) }
                    setOnMouseClicked {
                        ProductsView(onProductSelectedListener = object : OnProductSelectedListener {
                            override fun onProductSelected(productsTable: ObservableList<ProductsTable>?) {
                                invoiceViewModel.productsList.value = productsTable
                                invoiceViewModel.totalPrice.value = productsTable?.map { it.amount }?.sum().toString()
                                invoiceViewModel.payableAmount.value = productsTable?.map { it.amount }?.sum().toString()
                                invoicesController.updateProductsObserver(productsTable)
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

            tableview(invoicesController.productsListObserver) {
                this.minWidth = Screen.getPrimary().visualBounds.width / 3
                tag = "products"
                vboxConstraints { margin = Insets(10.0) }
                column("ID", ProductsTable::productId)
                column("Product Name", ProductsTable::productName)
                column("Amount", ProductsTable::amount)
            }
        }
    }

    private fun showInvoiceDetails(selectedItem: InvoiceTable?) {
        selectedItem?.let {
            invoicesController.getCustomerById(selectedItem.customerId).subscribe { t1, t2 ->
                t1?.let { customer ->
                    InvoiceDetailView(it, customer).openWindow()
                }
            }
        }
    }

    fun requestForInvoices() {
        invoicesController.requestForInvoices()
    }
}