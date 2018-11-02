package com.dashlabs.invoicemanagement.view.dashboard

import com.dashlabs.invoicemanagement.app.InvoiceApp
import com.dashlabs.invoicemanagement.view.admin.AdminLoginView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.stage.Modality
import javafx.stage.StageStyle
import tornadofx.*

class DashboardView : View("Invoice Dashboard") {

    private val dashboardController: DashboardController by inject()

    init {
        subscribe<InvoiceApp.AdminLoggedInEvent> {
            dashboardController.adminLoggedin(it.admin)
            println("User logged in! ${it.admin.username}")
        }
    }
    override val root = vbox {
        minHeight = 300.0
        minWidth = 500.0
        alignment = Pos.CENTER
        label("Dummy Trading Company\nRoad no.12 Banjara Hills\nHyderabad 500034") {
            alignment = Pos.TOP_CENTER
            paddingAll = 10.0
        }

        label(dashboardController.statusProperty) {
            alignment = Pos.TOP_RIGHT
            paddingAll = 10.0
        }

        hbox {
            alignment = Pos.CENTER
            button(text = "Products") {
                hboxConstraints {
                    marginRight = 20.0
                    hGrow = Priority.ALWAYS
                }
                setOnMouseClicked {

                }
            }

            button(text = "Customers") {
                hboxConstraints {
                    marginRight = 20.0
                    hGrow = Priority.ALWAYS
                }
                setOnMouseClicked {

                }
            }

            button(dashboardController.admingSettingsProperty) {
                hboxConstraints {
                    marginRight = 20.0
                    hGrow = Priority.ALWAYS
                }
                setOnMouseClicked {
                    (app as InvoiceApp).admin?.let {
                        openInternalWindow(AdminSettingsView::class)
                    }?:kotlin.run {
                        openInternalWindow(AdminLoginView::class)
                    }
                }
            }

            button(text = "Invoices") {
                hboxConstraints {
                    marginRight = 20.0
                    hGrow = Priority.ALWAYS
                }
                setOnMouseClicked {

                }
            }
        }
    }
}