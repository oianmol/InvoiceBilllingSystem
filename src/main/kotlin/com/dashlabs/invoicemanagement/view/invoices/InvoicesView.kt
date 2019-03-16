package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.dashlabs.invoicemanagement.view.customers.CustomersView
import com.dashlabs.invoicemanagement.view.customers.OnCustomerSelectedListener
import com.dashlabs.invoicemanagement.view.customers.OnProductSelectedListener
import com.dashlabs.invoicemanagement.view.products.ProductsView
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination.SHIFT_DOWN
import org.fxmisc.wellbehaved.event.EventPattern.keyPressed
import org.fxmisc.wellbehaved.event.InputMap.consume
import org.fxmisc.wellbehaved.event.InputMap.sequence
import org.fxmisc.wellbehaved.event.Nodes
import tornadofx.*


class InvoicesView : View("Invoices View"), OnProductSelectedListener, OnCustomerSelectedListener {

    private val invoiceViewModel = InvoiceViewModel()
    private val invoicesController: InvoicesController by inject()
    private var productsTableView: TableView<InvoicesController.ProductsModel>? = null

    override val root = vbox {
        Nodes.addInputMap(this, sequence(
                consume(keyPressed(KeyCode.C, SHIFT_DOWN)) { e ->
                    CustomersView(onCustomerSelectedListener = this@InvoicesView).openWindow()
                },
                consume(keyPressed(KeyCode.P, SHIFT_DOWN)) { e ->
                    ProductsView(onProductSelectedListener = this@InvoicesView).openWindow()
                }
        ))
        hbox {
            vboxConstraints { margin = Insets(20.0) }

            vbox {
                hboxConstraints { margin = Insets(20.0) }

                label(invoiceViewModel.customer) {
                    vboxConstraints { margin = Insets(10.0, 0.0, 0.0, 0.0) }
                }

                button("Select Customer (Shift+C)") {
                    vboxConstraints { margin = Insets(10.0, 0.0, 0.0, 0.0) }
                    action {
                        CustomersView(onCustomerSelectedListener = this@InvoicesView).openWindow()
                    }
                }

                button("Select Products (Shift+P)") {
                    vboxConstraints { margin = Insets(10.0, 0.0, 0.0, 00.0) }
                    action {
                        ProductsView(onProductSelectedListener = this@InvoicesView).openWindow()
                    }
                }
            }

            vbox {
                hboxConstraints { margin = Insets(20.0) }
                /*vbox {
                    isVisible = false
                    vboxConstraints { margin = Insets(0.0, 0.0, 0.0, 10.0) }
                    label("Total Amount: ") {
                        vboxConstraints { margin = Insets(10.0) }
                    }

                    textfield(invoiceViewModel.totalPrice) {
                        this.isEditable = false
                        this.filterInput { it.controlNewText.isDouble() }
                    }
                }*/

                vbox {
                    vboxConstraints { margin = Insets(0.0, 0.0, 0.0, 10.0) }

                    label("Paying Amount: ") {
                        vboxConstraints { margin = Insets(10.0) }
                    }

                    textfield(invoiceViewModel.payingAmount) {
                        this.filterInput {
                            it.controlNewText.isDouble() && it.controlNewText.toDouble() <= invoiceViewModel.totalPrice.value.toDouble()
                        }
                    }.textProperty().addListener { observable, oldValue, newValue ->
                        try {
                            newValue.takeIf { !it.isNullOrEmpty() }?.let {
                                val bal = invoiceViewModel.totalPrice.value.toDouble().minus(it.toDouble())
                                invoiceViewModel.leftoverAmount.value = bal
                            } ?: kotlin.run {
                                val bal = invoiceViewModel.totalPrice.value.toDouble()
                                invoiceViewModel.leftoverAmount.value = bal
                            }
                        } catch (ex: Exception) {

                        }
                    }

                }

                vbox {
                    vboxConstraints { margin = Insets(0.0, 0.0, 0.0, 10.0) }

                    label("Amount Due: ") {
                        vboxConstraints { margin = Insets(10.0) }
                    }

                    textfield(invoiceViewModel.leftoverAmount) {
                        this.isEditable = false
                    }

                }

                button("Create Invoice") {
                    vboxConstraints { margin = Insets(10.0) }
                    setOnMouseClicked {
                        if (invoiceViewModel.customerId.value == 0L || invoicesController.productsQuanityView.isEmpty()) {
                            warning("Select products and customer first!").show()
                            return@setOnMouseClicked
                        } else {
                            if (invoiceViewModel.leftoverAmount.value != null && invoiceViewModel.leftoverAmount.value.toDouble() > 0) {
                                alert(Alert.AlertType.CONFIRMATION, "Credit Amount",
                                        "You are not paying in full and amount ${invoiceViewModel.leftoverAmount.value} will be added to your credits",
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

        vbox {
            vboxConstraints { margin = Insets(20.0) }
            tableview<InvoicesController.ProductsModel>(invoicesController.productsQuanityView) {
                isEditable = true
                stylesheets.add("jfx-table-view.css")
                this@InvoicesView.productsTableView = this
                column("Product Name", InvoicesController.ProductsModel::productsTable)
                column("Quantity", InvoicesController.ProductsModel::quantity).makeEditable().setOnEditCommit {
                    if (it.newValue.toInt() != 0) {
                        invoicesController.productsQuanityView[it.tablePosition.row].quantity = it.newValue
                        invoicesController.productsQuanityView[it.tablePosition.row].totalAmount = it.newValue.toInt().times(invoicesController.productsQuanityView[it.tablePosition.row].baseAmount).toString()
                        updateTotalAmount()
                    }
                    refresh()
                }
                column("Amount", InvoicesController.ProductsModel::totalAmount)
                columnResizePolicy = SmartResize.POLICY

                this.setOnKeyPressed {
                    when (it.code) {
                        KeyCode.DELETE, KeyCode.BACK_SPACE -> {
                            selectedCell?.row?.let { position ->
                                alert(Alert.AlertType.CONFIRMATION, "Remove Items ?",
                                        "Remove ${selectedItem?.productsTable?.productName} ?",
                                        buttons = *arrayOf(ButtonType.OK, ButtonType.CANCEL), owner = currentWindow, title = "Hey!") {
                                    if (it == ButtonType.OK) {
                                        invoicesController.productsQuanityView.removeAt(position)
                                        refresh()
                                    }
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

    override fun onCustomerSelected(customersTable: CustomersTable.MeaningfulCustomer) {
        invoiceViewModel.customerId.value = customersTable.customerId
        invoiceViewModel.customer.value = customersTable
    }

    override fun onProductSelected(newSelectedProducts: ObservableMap<ProductsTable, Int>?) {
        val currentList = FXCollections.observableArrayList<InvoicesController.ProductsModel>()
        invoicesController.productsQuanityView?.let {
            currentList.setAll(it)
        }
        newSelectedProducts?.forEach { item ->
            item.key?.let {
                val product = currentList.containsProduct(item.key)
                if (product != -1) {
                    currentList[product].quantity = currentList[product].quantity.toInt().plus(1).toString()
                } else {
                    currentList.add(InvoicesController.ProductsModel(item.key, "1", item.key.amount.toString(), item.key.amount))
                }
            }
        }
        invoicesController.updateProductsObserver(currentList)

        invoiceViewModel.productsList.value = currentList
        invoicesController.productsQuanityView.setAll(currentList)
        val totalAmount = invoicesController.productsQuanityView.map { it.productsTable.amount * it.quantity.toInt() }.sum()
        invoiceViewModel.totalPrice.value = totalAmount
        invoiceViewModel.leftoverAmount.value = totalAmount

        Platform.runLater {
            productsTableView?.refresh()
        }
    }


    private fun updateTotalAmount() {
        Platform.runLater {
            val totalAmount = invoicesController.productsQuanityView.map { it.totalAmount.toDouble() }.sum()
            invoiceViewModel.totalPrice.value = totalAmount
            invoiceViewModel.leftoverAmount.value = totalAmount
        }
    }

    fun requestForInvoices() {
        invoicesController.requestForInvoices()
    }
}

private fun ObservableList<InvoicesController.ProductsModel>.containsProduct(key: ProductsTable): Int {
    var index = -1
    for (e in this) {
        index++
        if (e.productsTable.productId == key.productId) {
            return index
        }
    }
    return -1
}
