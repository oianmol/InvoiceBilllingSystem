package com.dashlabs.invoicemanagement.view.dashboard

import com.dashlabs.invoicemanagement.app.InvoiceApp
import com.dashlabs.invoicemanagement.view.admin.AdminLoginView
import com.dashlabs.invoicemanagement.view.customers.CustomersView
import com.dashlabs.invoicemanagement.view.products.ProductsView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TabPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*

class DashboardView : View("Dashboard") {

    private val dashboardController: DashboardController by inject()
    private var tabNames: ArrayList<String> = arrayListOf("Products", "Customers", "Invoices")
    private var productsView: ProductsView = ProductsView()
    private var customersView: CustomersView = CustomersView()

    init {
        subscribe<InvoiceApp.AdminLoggedInEvent> {
            dashboardController.adminLoggedin(it.admin)
            productsView.requestForProducts()
            customersView.requestForCustomers()
            println("User logged in! ${it.admin.username}")
        }
    }

    override val root = vbox {
        minHeight = 400.0
        minWidth = 600.0
        hbox {
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
            }
        }
    }

    private fun isAdminLoggedIn(): Boolean {
        return (app as InvoiceApp).admin != null
    }

}