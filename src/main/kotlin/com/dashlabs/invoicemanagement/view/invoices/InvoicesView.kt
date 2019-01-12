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
import javafx.scene.layout.VBox
import tornadofx.*

class InvoicesView : View("Invoices View") {

    private val invoiceViewModel = InvoiceViewModel()
    private val invoicesController: InvoicesController by inject()
    private var productsView: VBox? = null

    override val root = scrollpane {
        content = vbox {
            vboxConstraints { margin = Insets(10.0) }

            hbox {
                vbox {
                    this.add(customerView())
                    this.add(getProductsView())
                }

                this.add(createInvoiceAndList())
            }


        }
    }

    private fun createInvoiceAndList(): VBox {
        return vbox {
            vboxConstraints { margin = Insets(10.0) }

            tableview<InvoiceTable>(invoicesController.invoicesListObserver) {
                vboxConstraints { margin = Insets(10.0) }
                tag = "invoices"
                column("Date Created", InvoiceTable::formattedCreatedDate)
                column("Customer Name Id", InvoiceTable::getCustomerNameId)
            }
        }
    }

    private fun getProductsView(): VBox {
        return vbox {
            this@InvoicesView.productsView = this
            vboxConstraints { margin = Insets(10.0) }

            button("Select Products") {
                vboxConstraints { margin = Insets(10.0) }
                setOnMouseClicked {
                    ProductsView(onProductSelectedListener = object : OnProductSelectedListener {
                        override fun onProductSelected(productsTable: ObservableList<ProductsTable>?) {
                            invoiceViewModel.productsList.value = productsTable
                            invoicesController.updateProductsObserver(productsTable)
                        }
                    }).openWindow()
                }
            }

            tableview(invoicesController.productsListObserver) {
                tag = "products"
                vboxConstraints { margin = Insets(10.0) }
                column("ID", ProductsTable::productId)
                column("Product Name", ProductsTable::productName)
                column("Amount", ProductsTable::amount)
            }

            button("Create Invoice") {
                vboxConstraints { margin = Insets(10.0) }
                setOnMouseClicked {
                    if (invoiceViewModel.customerId.value == 0L || invoiceViewModel.productsList.value.isEmpty()) {
                        warning("Select products and customer first!").show()
                        return@setOnMouseClicked
                    } else {
                        invoicesController.addInvoice(invoiceViewModel)
                    }
                }
            }
        }
    }

    private fun customerView(): VBox {

        return vbox {
            vboxConstraints { margin = Insets(10.0) }
            button("Select Customer") {
                vboxConstraints { margin = Insets(10.0) }
                setOnMouseClicked {
                    CustomersView(onCustomerSelectedListener = object : OnCustomerSelectedListener {
                        override fun onCustomerSelected(customersTable: CustomersTable) {
                            invoiceViewModel.customerId.value = customersTable.customerId
                            invoiceViewModel.customer.value = customersTable
                        }

                    }).openWindow()
                }
            }

            label(invoiceViewModel.customer)
        }
    }


    fun requestForInvoices() {
        invoicesController.requestForInvoices()
    }
}