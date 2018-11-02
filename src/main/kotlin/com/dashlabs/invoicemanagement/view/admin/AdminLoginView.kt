package com.dashlabs.invoicemanagement.view.admin

import com.dashlabs.invoicemanagement.app.InvoiceApp
import com.dashlabs.invoicemanagement.databaseconnection.AdminTable
import com.dashlabs.invoicemanagement.view.dashboard.DashboardView
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import javafx.stage.Modality
import javafx.stage.StageStyle
import tornadofx.*

class AdminLoginView : View("Admin Login!") {

    private val adminModel = AdminModel()

    override val root = form {
        fieldset(title, labelPosition = Orientation.VERTICAL) {
            field("Admin Username") {
                textfield(adminModel.username).validator {
                    if (it.isNullOrBlank()) error("The username field is required") else null
                }
            }
            field("Admin Password") {
                passwordfield(adminModel.password).validator {
                    if (it.isNullOrBlank()) error("The password field is required") else null
                }
            }

            hbox {
                button("Login!") {
                    hboxConstraints {
                        marginRight = 20.0
                        hGrow = Priority.ALWAYS
                    }
                    action {
                        adminModel.loginUser()
                                .subscribeOn(Schedulers.io())
                                .observeOn(JavaFxScheduler.platform())
                                .subscribe { t1, t2 ->
                                    t1?.let {
                                        information("Logged In! ${it.name}")
                                        onLoginDashboard(it)
                                    }
                                    t2?.let {
                                        it.message?.let { it1 -> information(it1) }
                                    }
                                }
                    }
                }

                button("Register Admin") {
                    hboxConstraints {
                        marginRight = 20.0
                        hGrow = Priority.ALWAYS
                    }
                    action {
                        adminModel.registerUser()
                                .subscribeOn(Schedulers.io())
                                .observeOn(JavaFxScheduler.platform())
                                .subscribe { t1, t2 ->
                                    t1?.let {
                                        information("Registered User! ${it.name}")
                                        onLoginDashboard(it)
                                    }
                                    t2?.let {
                                        it.message?.let { it1 -> information(it1) }
                                    }
                                }
                    }
                }
            }
        }
    }

    private fun onLoginDashboard(it: AdminTable) {
        val myApp = app as InvoiceApp
        myApp.setUser(it)
        find<DashboardView> {
            this.close()
        }.openWindow(stageStyle = StageStyle.DECORATED,modality = Modality.WINDOW_MODAL,escapeClosesWindow = false,owner = this@AdminLoginView.currentWindow,block = false,resizable = true)
    }
}