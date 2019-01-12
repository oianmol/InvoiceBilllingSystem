package com.dashlabs.invoicemanagement.view.customers

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import tornadofx.*

class CustomersView(private val onCustomerSelectedListener: OnCustomerSelectedListener? = null) : View("Customers View") {

    private val customersViewModel = CustomerViewModel()
    private val customersController: CustomersController by inject()

    override val root = hbox {
        this.add(getCustomersView())
    }

    private fun getCustomersView(): VBox {
        return vbox {
            hbox {
                this.add(getSearchProductForm())
                this.add(getAddProductView())
            }
            tableview<CustomersTable>(customersController.customersListObserver) {
                column("ID", CustomersTable::customerId)
                column("Customer Name", CustomersTable::customerName)
                column("Date Created", CustomersTable::dateCreated)
                column("Aadhar Card", CustomersTable::aadharCard)
                column("Balance", CustomersTable::balance)
                onDoubleClick {
                    onCustomerSelectedListener?.let {
                        it.onCustomerSelected(this.selectedItem!!)
                        currentStage?.close()
                    }
                }
            }
        }
    }

    private fun getAddProductView(): VBox {
        return vbox {
            form {
                fieldset {
                    field("Customer Name") {
                        textfield(customersViewModel.customerName).validator {
                            if (it.isNullOrBlank()) error("Please enter customer name!") else null
                        }
                    }

                    field("Aadhar Number") {
                        textfield(customersViewModel.aadharNumber) {
                            this.filterInput { it.controlNewText.isLong() }
                        }.validator {
                            if (it.isNullOrBlank()) error("Please enter aadhar number!") else null
                        }
                    }

                    field("Age") {
                        textfield(customersViewModel.age) {
                            this.filterInput { it.controlNewText.isInt() }
                        }.validator {
                            if (it.isNullOrBlank()) error("Please enter Age") else null
                        }
                    }


                    field("Amount") {
                        textfield(customersViewModel.balance) {
                            this.filterInput { it.controlNewText.isDouble() }
                        }.validator {
                            if (it.isNullOrBlank()) error("Please specify amount!") else null
                        }
                    }
                }



                button("Add Customer") {
                    setOnMouseClicked {
                        customersController.addCustomer(customersViewModel.customerName, customersViewModel.aadharNumber, customersViewModel.age, customersViewModel.balance)
                    }
                }
            }

        }
    }

    private fun getSearchProductForm(): VBox {
        return vbox {
            form {
                fieldset {
                    field("Search Customers") {
                        textfield(customersViewModel.searchName).validator {
                            if (it.isNullOrBlank()) error("Please enter search Query!") else null
                        }
                    }
                }

                button("Search Customer") {
                    alignment = Pos.BOTTOM_RIGHT
                    setOnMouseClicked {
                        customersController.searchProduct(customersViewModel.searchName)
                    }
                }
            }

        }
    }

    fun requestForCustomers() {
        customersController.requestForCustomers()
    }
}