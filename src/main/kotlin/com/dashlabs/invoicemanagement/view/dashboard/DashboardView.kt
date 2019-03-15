package com.dashlabs.invoicemanagement.view.dashboard

import com.dashlabs.invoicemanagement.app.InvoiceApp
import com.dashlabs.invoicemanagement.view.admin.AdminLoginView
import com.dashlabs.invoicemanagement.view.admin.ChangePasswordView
import com.dashlabs.invoicemanagement.view.customers.CustomersView
import com.dashlabs.invoicemanagement.view.customers.SearchCustomerView
import com.dashlabs.invoicemanagement.view.invoices.InvoicesView
import com.dashlabs.invoicemanagement.view.invoices.SearchInvoiceView
import com.dashlabs.invoicemanagement.view.products.ProductsView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.fxmisc.wellbehaved.event.EventPattern
import org.fxmisc.wellbehaved.event.InputMap
import org.fxmisc.wellbehaved.event.Nodes
import tornadofx.*


class DashboardView : View("Dashboard") {

    private val dashboardController: DashboardController by inject()
    private var tabNames: ArrayList<String> = arrayListOf("Products [1]", "Customers [2]", "Create Invoice [3]", "Invoice Search [4]", "Area Wise Customer's Search[5]")
    private var productsView: ProductsView = ProductsView()
    private var customersView: CustomersView = CustomersView()
    private var invoicesView = InvoicesView()
    private var invoicesSearchView = SearchInvoiceView()
    private var customerSearch = SearchCustomerView()

    init {
        subscribe<InvoiceApp.AdminLoggedInEvent> {
            dashboardController.adminLoggedin(it.admin)
            productsView.requestForProducts()
            customersView.requestForCustomers()
            invoicesView.requestForInvoices()
            invoicesSearchView.requestForInvoices()
            println("User logged in! ${it.admin.username}")
        }
    }

    override val root = hbox {

        Nodes.addInputMap(this, InputMap.sequence(
                InputMap.consume(EventPattern.keyPressed(KeyCode.L, KeyCombination.CONTROL_DOWN)) { e ->
                    if (!isAdminLoggedIn()) {
                        openInternalWindow(AdminLoginView::class)
                    } else {
                        ChangePasswordView(dashboardController.admin).openWindow()
                    }
                },
                InputMap.consume(EventPattern.keyPressed(KeyCode.C, KeyCombination.CONTROL_DOWN)) { e ->
                    if (!isAdminLoggedIn()) {
                        openInternalWindow(AdminLoginView::class)
                    } else {
                        ChangePasswordView(dashboardController.admin).openWindow()
                    }
                }
        ))

        this.hgrow = Priority.ALWAYS
        this.add(imageview("nfs.jpg", lazyload = true) {
            minWidth = 1200.0
            minHeight = 628.0
            onDoubleClick {
                this.removeFromParent()
                this@hbox.add(getMainView())
            }
        })
    }

    private fun getMainView(): VBox {
        return vbox {
            this.vgrow = Priority.ALWAYS
            hbox {
                label(dashboardController.statusProperty) {
                    alignment = Pos.TOP_RIGHT
                    paddingAll = 10.0
                    HBox.setMargin(this, Insets(10.0))
                }

                button(dashboardController.admingSettingsProperty) {
                    HBox.setMargin(this, Insets(10.0))
                    hboxConstraints {
                        marginRight = 20.0
                        hGrow = Priority.ALWAYS
                    }
                    setOnMouseClicked {
                        if (!isAdminLoggedIn()) {
                            openInternalWindow(AdminLoginView::class)
                        } else {
                            ChangePasswordView(dashboardController.admin).openWindow()
                        }
                    }
                }
            }


            drawer {
                this.scene?.setOnKeyPressed {
                    when (it.code) {
                        KeyCode.DIGIT1 -> {
                            this.items[0].expanded = true
                        }
                        KeyCode.DIGIT2 -> {
                            this.items[1].expanded = true
                        }
                        KeyCode.DIGIT3 -> {
                            this.items[2].expanded = true
                        }
                        KeyCode.DIGIT4 -> {
                            this.items[3].expanded = true
                        }
                        KeyCode.DIGIT5 -> {
                            this.items[4].expanded = true
                        }
                        else -> {
                        }
                    }
                }


                minWidth = 1200.0
                minHeight = 628.0
                dockingSide = Side.TOP
                enableWhen(dashboardController.adminLogin)
                item(tabNames[0], expanded = true) {
                    this.add(productsView)
                }

                item(tabNames[1]) {
                    this.add(customersView)
                }

                item(tabNames[2]) {
                    this.add(invoicesView)
                }
                item(tabNames[3]) {
                    this.add(invoicesSearchView)
                }
                item(tabNames[4]) {
                    this.add(customerSearch)
                }
            }
        }
    }

    private fun isAdminLoggedIn(): Boolean {
        return (app as InvoiceApp).admin != null
    }

}