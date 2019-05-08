package com.dashlabs.invoicemanagement.view.customers

import com.dashlabs.invoicemanagement.State
import com.dashlabs.invoicemanagement.StateDistrict
import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.google.gson.Gson
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.layout.VBox
import javafx.stage.Screen
import org.fxmisc.wellbehaved.event.EventPattern
import org.fxmisc.wellbehaved.event.InputMap
import org.fxmisc.wellbehaved.event.Nodes
import tornadofx.*
import javax.json.Json

class CustomersView(private val onCustomerSelectedListener: OnCustomerSelectedListener? = null) : View("Customers View") {

    private val customersViewModel = CustomerViewModel()
    private val customersController: CustomersController by inject()
    val state = getStates()
    private var districtView: Field? = null
    private lateinit var customerName: TextField

    override fun onDock() {
        super.onDock()
        repeatFocus(customerName)
    }

    private fun repeatFocus(node: Node) {
        Platform.runLater {
            if (!node.isFocused) {
                node.requestFocus()
                repeatFocus(node)
            }
        }
    }

    override val root = hbox {
        Nodes.addInputMap(this, InputMap.sequence(
                InputMap.consume(EventPattern.keyPressed(KeyCode.A, KeyCombination.CONTROL_DOWN)) { e ->
                    customersController.addCustomer(customersViewModel.customerName,
                            customersViewModel.address,
                            customersViewModel.state,
                            customersViewModel.district)
                }
        ))


        this.add(getCustomersView())
        this.add(getCustomersList())
    }

    private fun getCustomersList(): VBox {
        return vbox {
            onCustomerSelectedListener?.let {

            }?:kotlin.run {
                Platform.runLater {
                    this.minWidth = Screen.getPrimary().visualBounds.width.div(3)
                }
            }

            tableview<CustomersTable.MeaningfulCustomer>(customersController.customersListObserver) {
                columnResizePolicy = SmartResize.POLICY
                vboxConstraints { margin = Insets(20.0) }
                stylesheets.add("jfx-table-view.css")

                column("Customer Name", CustomersTable.MeaningfulCustomer::customerName)
                column("Address", CustomersTable.MeaningfulCustomer::address)
                column("State", CustomersTable.MeaningfulCustomer::state)
                column("District", CustomersTable.MeaningfulCustomer::district)

                onDoubleClick {
                    onCustomerSelectedListener?.let {
                        it.onCustomerSelected(this.selectedItem!!)
                        currentStage?.close()
                    } ?: kotlin.run {
                        CustomerDetailView(this.selectedItem!!).openWindow()
                    }
                }

                this.setOnKeyPressed {
                    this.selectedItem?.let { item ->
                        when (it.code) {
                            KeyCode.ENTER -> {
                                onCustomerSelectedListener?.let {
                                    it.onCustomerSelected(this.selectedItem!!)
                                    currentStage?.close()
                                } ?: kotlin.run {
                                    CustomerDetailView(this.selectedItem!!).openWindow()
                                }
                            }
                            KeyCode.DELETE, KeyCode.BACK_SPACE -> {
                                alert(Alert.AlertType.CONFIRMATION, "Delete Customer ${this.selectedItem!!.customerName} ?",
                                        "Remove ${item.customerName} ?",
                                        buttons = *arrayOf(ButtonType.YES, ButtonType.CANCEL), owner = currentWindow, title = "Hey!") {
                                    if (it == ButtonType.YES) {
                                        customersController.deleteCustomer(item)
                                    }
                                }
                            }
                            else -> {

                            }
                        }
                    }

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
                        textfield(customersViewModel.customerName) {
                            tag = "customer"
                        }.validator {
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



                button("Add Customer (CTRL+A)") {
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
                        textfield(customersViewModel.searchName) {
                            this@CustomersView.customerName = this
                        }.validator {
                            if (it.isNullOrBlank()) error("Please enter search Query!") else null
                        }

                        customersViewModel.searchName.onChange {
                            it?.let {
                                customersController.searchProduct(it)
                            } ?: run {
                                requestForCustomers()
                            }
                        }
                    }
                }

                button("Search Customer") {
                    alignment = Pos.BOTTOM_RIGHT
                    setOnMouseClicked {
                        customersController.searchProduct(customersViewModel.searchName.value)
                    }
                }
            }

        }
    }

    fun requestForCustomers() {
        customersController.requestForCustomers()
    }
}