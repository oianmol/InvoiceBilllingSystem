package com.dashlabs.invoicemanagement.view.customers

import com.dashlabs.invoicemanagement.State
import com.dashlabs.invoicemanagement.StateDistrict
import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.google.gson.Gson
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import tornadofx.*
import javax.json.Json

class CustomersView(private val onCustomerSelectedListener: OnCustomerSelectedListener? = null) : View("Customers View") {

    private val customersViewModel = CustomerViewModel()
    private val customersController: CustomersController by inject()
    val state = getStates()
    private var districtView: Field? = null

    override val root = hbox {
        this.add(getCustomersView())

        tableview<CustomersTable>(customersController.customersListObserver) {
            columnResizePolicy = SmartResize.POLICY
            hboxConstraints { margin = Insets(20.0, 0.0, 0.0, 0.0) }

            column("Customer Name", CustomersTable::customerName)
            column("Address", CustomersTable::address)
            column("State", CustomersTable::state)
            column("District", CustomersTable::district)

            onDoubleClick {
                onCustomerSelectedListener?.let {
                    it.onCustomerSelected(this.selectedItem!!)
                    currentStage?.close()
                } ?: kotlin.run {
                    CustomerDetailView(this.selectedItem!!).openWindow()
                }
            }
        }
    }

    private fun getCustomersView(): VBox {
        return vbox {
            this.add(getSearchProductForm())
            this.add(getAddProductView())
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

                    field("Address") {
                        textfield(customersViewModel.address) {
                        }.validator {
                            if (it.isNullOrBlank()) error("Please enter address!") else null
                        }
                    }

                    field("State") {
                        combobox(customersViewModel.state, state.map { it.state }) {
                            selectionModel.selectedIndex
                        }.validator {
                            if (it.isNullOrBlank()) error("Please select the state") else null
                        }
                    }

                    customersViewModel.state.onChange {
                        this@CustomersView.districtView?.let {
                            customersViewModel.district.value = null
                            it.removeFromParent()
                        }
                        getDistrictView(this@fieldset)
                    }
                }



                button("Add Customer") {
                    setOnMouseClicked {
                        customersController.addCustomer(customersViewModel.customerName, customersViewModel.address, customersViewModel.state, customersViewModel.district)
                    }
                }
            }

        }
    }

    private fun getDistrictView(it: Fieldset) {
        this@CustomersView.districtView = field("District") {
            tag = "district"
            val state = state.firstOrNull { it.state.equals(customersViewModel.state.value) }
            state?.let {
                combobox(customersViewModel.district, state.districts).validator {
                    if (it.isNullOrBlank()) error("Please select the District!") else null
                }
            }
        }
        this@CustomersView.districtView?.addTo(it)
    }

    private fun getStates(): List<State> {
        val stream = javaClass.getResourceAsStream("/states-and-districts.json")
        val state = Gson().fromJson(Json.createReader(stream).readObject().toPrettyString(), StateDistrict::class.java)
        return state.states
    }

    private fun getSearchProductForm(): VBox {
        return vbox {
            vboxConstraints { margin = Insets(20.0, 0.0, 20.0, 0.0) }
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