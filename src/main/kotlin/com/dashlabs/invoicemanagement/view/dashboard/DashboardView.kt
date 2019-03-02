package com.dashlabs.invoicemanagement.view.dashboard

import com.dashlabs.invoicemanagement.app.InvoiceApp
import com.dashlabs.invoicemanagement.view.admin.AdminLoginView
import com.dashlabs.invoicemanagement.view.customers.CustomersView
import com.dashlabs.invoicemanagement.view.invoices.InvoicesView
import com.dashlabs.invoicemanagement.view.products.ProductsView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TabPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Screen
import tornadofx.*
import javafx.stage.Screen.getPrimary



class DashboardView : View("Dashboard") {

    private val dashboardController: DashboardController by inject()
    private var tabNames: ArrayList<String> = arrayListOf("Products", "Customers", "Invoices")
    private var productsView: ProductsView = ProductsView()
    private var customersView: CustomersView = CustomersView()
    private var invoicesView = InvoicesView()

    init {
        subscribe<InvoiceApp.AdminLoggedInEvent> {
            dashboardController.adminLoggedin(it.admin)
            productsView.requestForProducts()
            customersView.requestForCustomers()
            invoicesView.requestForInvoices()
            println("User logged in! ${it.admin.username}")
        }
    }

    override val root = hbox {
        this.hgrow = Priority.ALWAYS
        this.add(imageview("nfs.jpg",lazyload = true){
            val primScreenBounds = Screen.getPrimary().visualBounds
            minWidth = primScreenBounds.width
            minHeight = primScreenBounds.height
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
                this.minWidth = Screen.getPrimary().visualBounds.width
                label("Company Name \nRoad no.12 Banjara Hills\nHyderabad 500034") {
                    alignment = Pos.TOP_LEFT
                    paddingAll = 10.0
                    HBox.setMargin(this, Insets(10.0))
                }
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
                        if (isAdminLoggedIn()) {
                            openInternalWindow(AdminSettingsView::class)
                        } else {
                            openInternalWindow(AdminLoginView::class)
                        }
                    }
                }
            }


            tabpane {
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                enableWhen(dashboardController.adminLogin)
                tab(tabNames[0]) {
                    this.add(productsView)
                }

                tab(tabNames[1]) {
                    this.add(customersView)
                }

                tab(tabNames[2]) {
                    this.add(invoicesView)
                }
            }
        }
    }

    private fun isAdminLoggedIn(): Boolean {
        return (app as InvoiceApp).admin != null
    }

}