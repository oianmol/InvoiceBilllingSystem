package com.dashlabs.invoicemanagement.view.dashboard

import com.dashlabs.invoicemanagement.app.InvoiceApp
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.dashlabs.invoicemanagement.view.admin.AdminLoginView
import com.dashlabs.invoicemanagement.view.customers.CustomersView
import com.dashlabs.invoicemanagement.view.products.ProductViewModel
import com.dashlabs.invoicemanagement.view.products.ProductsController
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TabPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*

class DashboardView : View("Dashboard") {

    private val dashboardController: DashboardController by inject()
    private val productsController: ProductsController by inject()
    private val productViewModel = ProductViewModel()
    private var tabNames: ArrayList<String> = arrayListOf("Products", "Customers", "Invoices")

    init {
        subscribe<InvoiceApp.AdminLoggedInEvent> {
            dashboardController.adminLoggedin(it.admin)
            productsController.requestForProducts()
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
                vbox {
                    hbox {
                        vbox {
                            form {
                                fieldset {
                                    field("Search Products") {
                                        textfield(productViewModel.searchName).validator {
                                            if (it.isNullOrBlank()) error("Please enter search Query!") else null
                                        }
                                    }
                                }



                                button("Search Product") {
                                    alignment = Pos.BOTTOM_RIGHT
                                    setOnMouseClicked {
                                        productsController.searchProduct(productViewModel.searchName)
                                    }
                                }
                            }

                        }

                        vbox {
                            form {
                                fieldset {
                                    field("Product Name") {
                                        textfield(productViewModel.productName).validator {
                                            if (it.isNullOrBlank()) error("Please enter product name!") else null
                                        }
                                    }

                                    field("Amount") {
                                        textfield(productViewModel.amountName){
                                            this.filterInput { it.controlNewText.isDouble() }
                                        }.validator {
                                            if (it.isNullOrBlank()) error("Please specify amount!") else null
                                        }
                                    }
                                }



                                button("Add Product") {
                                    setOnMouseClicked {
                                        productsController.addProduct(productViewModel.productName, productViewModel.amountName)
                                    }
                                }
                            }

                        }


                    }
                    tableview<ProductsTable>(productsController.productsListObserver) {
                        column("ID", ProductsTable::productId)
                        column("Product Name", ProductsTable::productName)
                        column("Amount", ProductsTable::amount)
                    }
                }
            }

            tab(tabNames[1]) {
                this.add(CustomersView())
            }

            tab(tabNames[2]) {
            }
        }
    }

    private fun isAdminLoggedIn(): Boolean {
        return (app as InvoiceApp).admin != null
    }

}