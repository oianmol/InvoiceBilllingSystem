package com.dashlabs.invoicemanagement.view.invoices

import javafx.geometry.Pos
import javafx.scene.layout.VBox
import tornadofx.*

class InvoicesView : View("Invoices View") {

    private val invoiceViewModel = InvoiceViewModel()
    private val invoicesController: InvoicesController by inject()

    override val root = hbox {
        this.add(getInvoicesView())
    }

    private fun getInvoicesView(): VBox {
        return vbox {
            hbox {
                this.add(getSearchInvoiceForm())
                this.add(getAddInvoiceView())
            }
        }
    }

    private fun getAddInvoiceView(): VBox {
        return vbox {
            form {
                fieldset {
                    field("Search Customer Name") {
                        textfield(invoiceViewModel.searchCustomerName).validator {
                            if (it.isNullOrBlank()) error("Please enter customer name!") else null
                        }
                    }
                }



                button("Add Invoice") {
                    setOnMouseClicked {
                        invoicesController.addInvoice(invoiceViewModel.customerId, invoiceViewModel.productsList)
                    }
                }
            }

        }
    }

    private fun getSearchInvoiceForm(): VBox {
        return vbox {
            form {
                fieldset {
                    field("Search Invoice by customer name") {
                        textfield(invoiceViewModel.searchCustomerName).validator {
                            if (it.isNullOrBlank()) error("Please enter customer name!") else null
                        }
                    }
                }

                button("Search Invoice") {
                    alignment = Pos.BOTTOM_RIGHT
                    setOnMouseClicked {
                        invoicesController.searchInvoice(invoiceViewModel.searchCustomerName)
                    }
                }
            }

        }
    }

    fun requestForInvoices() {
        invoicesController.requestForInvoices()
    }
}