package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.dashlabs.invoicemanagement.view.customers.CustomersView
import com.dashlabs.invoicemanagement.view.customers.OnCustomerSelectedListener
import com.dashlabs.invoicemanagement.view.customers.OnProductSelectedListener
import com.dashlabs.invoicemanagement.view.products.ProductsView
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.layout.VBox
import tornadofx.*

class InvoicesView : View("Invoices View") {

    private val invoiceViewModel = InvoiceViewModel()
    private val invoicesController: InvoicesController by inject()

    override val root = hbox {
        this.add(vbox {
            hbox {
                this.add(getAddInvoiceView())
            }
        })
    }

    private fun getAddInvoiceView(): VBox {
        return vbox {
            form {
                button("Select Customer") {
                    vboxConstraints { margin = Insets(50.0) }
                    setOnMouseClicked {
                        CustomersView(onCustomerSelectedListener = object : OnCustomerSelectedListener {
                            override fun onCustomerSelected(customersTable: CustomersTable) {
                                invoiceViewModel.customerId.value = customersTable.customerId
                            }
                        }).openWindow()
                    }
                }

                button("Select Products") {
                    vboxConstraints { margin = Insets(50.0) }
                    setOnMouseClicked {
                        ProductsView(onProductSelectedListener = object : OnProductSelectedListener {
                            override fun onProductSelected(productsTable: MutableList<ProductsTable>?) {
                                invoiceViewModel.productsList.value = productsTable
                            }
                        }).openWindow()
                    }
                }

                button("Create Invoice") {
                    vboxConstraints { margin = Insets(50.0) }
                    setOnMouseClicked {
                        if (invoiceViewModel.customerId.value == 0L || invoiceViewModel.productsList.value.isEmpty()) {
                            warning("Select products and customer first!").show()
                            return@setOnMouseClicked
                        } else {
                            invoicesController.addInvoice(invoiceViewModel.customerId,
                                    invoiceViewModel.productsList)
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