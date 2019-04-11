package com.dashlabs.invoicemanagement.view.dashboard

import com.dashlabs.invoicemanagement.app.InvoiceApp
import com.dashlabs.invoicemanagement.view.admin.AdminLoginView
import com.dashlabs.invoicemanagement.view.admin.ChangePasswordView
import com.dashlabs.invoicemanagement.view.customers.CustomersView
import com.dashlabs.invoicemanagement.view.customers.SearchCustomerView
import com.dashlabs.invoicemanagement.view.invoices.InvoicesView
import com.dashlabs.invoicemanagement.view.invoices.SearchInvoiceView
import com.dashlabs.invoicemanagement.view.products.ProductsView
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTabPane
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TabPane
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
    private var tabNames: ArrayList<String> = arrayListOf("Products [CTRL+1]", "Customers [CTRL+2]", "Create Invoice [CTRL+3]", "Invoice Search [CTRL+4]", "Area Wise Customer's Search[CTRL+5]")
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

    override val root = stackpane {

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
                }, InputMap.consume(EventPattern.keyPressed(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN)) { e ->
            tabPane.tabs[0].select()
        },
                InputMap.consume(EventPattern.keyPressed(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN)) { e ->
                    tabPane.tabs[1].select()
                },
                InputMap.consume(EventPattern.keyPressed(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN)) { e ->
                    tabPane.tabs[2].select()
                },
                InputMap.consume(EventPattern.keyPressed(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN)) { e ->
                    tabPane.tabs[3].select()
                },
                InputMap.consume(EventPattern.keyPressed(KeyCode.DIGIT5, KeyCombination.CONTROL_DOWN)) { e ->
                    tabPane.tabs[4].select()
                },
                InputMap.consume(EventPattern.keyPressed(KeyCode.C, KeyCombination.SHIFT_DOWN)) { e ->
                    invoicesView.openCustomersView() },
                InputMap.consume(EventPattern.keyPressed(KeyCode.P, KeyCombination.SHIFT_DOWN)) { e ->
                    invoicesView.openProductsView()
                }
        ))

        this.add(getMainView())

        this.add(imageview("nfs.jpg", lazyload = true) {
            Platform.runLater {
                this.fitWidthProperty().bind(this@stackpane.scene.widthProperty())
                this.fitHeightProperty().bind(this@stackpane.scene.heightProperty())
            }

            onDoubleClick {
                this.isVisible = false
            }
        })

    }

    private lateinit var tabPane: JFXTabPane

    private fun getMainView(): VBox {
        return vbox {
            tag = "mainview"
            this.vgrow = Priority.ALWAYS
            hbox {
                label(dashboardController.statusProperty) {
                    alignment = Pos.TOP_RIGHT
                    paddingAll = 10.0
                    HBox.setMargin(this, Insets(10.0))
                }

                this += JFXButton().apply {
                    this.text(dashboardController.admingSettingsProperty) {

                    }
                    style = "   -jfx-button-type: RAISED;\n" +
                            "     -fx-background-color: #2196f3;\n" +
                            "     -fx-text-fill: white;"
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

            this += JFXTabPane().apply {
                this@DashboardView.tabPane = this
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
                tab(tabNames[3]) {
                    this.add(invoicesSearchView)
                }
                tab(tabNames[4]) {
                    this.add(customerSearch)
                }
            }

        }
    }

    private fun isAdminLoggedIn(): Boolean {
        return (app as InvoiceApp).admin != null
    }

}