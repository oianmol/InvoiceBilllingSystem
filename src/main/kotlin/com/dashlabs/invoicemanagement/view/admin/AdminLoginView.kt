package com.dashlabs.invoicemanagement.view.admin

import com.dashlabs.invoicemanagement.app.InvoiceApp
import com.dashlabs.invoicemanagement.databaseconnection.AdminTable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.fxmisc.wellbehaved.event.EventPattern
import org.fxmisc.wellbehaved.event.InputMap
import org.fxmisc.wellbehaved.event.Nodes
import tornadofx.*

class AdminLoginView : View("Admin Login!") {

    private val adminModel = AdminModel()

    private lateinit var registerLayout: HBox

    private lateinit var userName: Node
    private lateinit var registerButton: Button

    override fun onDock() {
        super.onDock()
        Platform.runLater {
            repeatFocus(userName)
        }
    }

    private fun repeatFocus(node: Node) {
        Platform.runLater {
            if (!node.isFocused()) {
                node.requestFocus()
                repeatFocus(node)
            }
        }
    }

    override val root = hbox {
        Nodes.addInputMap(this, InputMap.sequence(
                InputMap.consume(EventPattern.keyPressed(KeyCode.R, KeyCombination.SHIFT_DOWN)) { e ->
                    registerButton.isVisible = !registerButton.isVisible
                }
        ))
        form {
            fieldset(title, labelPosition = Orientation.VERTICAL) {
                field("Admin Username") {
                    textfield(adminModel.username){
                        this@AdminLoginView.userName = this
                    }.validator {
                        if (it.isNullOrBlank()) error("The username field is required") else null
                    }
                }
                field("Admin Password") {
                    passwordfield(adminModel.password).validator {
                        if (it.isNullOrBlank()) error("The password field is required") else null
                    }
                    this.setOnKeyPressed {
                        when (it.code) {
                            KeyCode.ENTER -> {
                                loginNow()
                            }
                        }
                    }
                }

                hbox {
                    this@AdminLoginView.registerLayout = this
                    button("Login!") {
                        hboxConstraints {
                            marginRight = 20.0
                            hGrow = Priority.ALWAYS
                        }
                        action {
                            loginNow()
                        }
                    }

                    button("Register Admin") {
                        this@AdminLoginView.registerButton = this
                        isVisible = false
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
    }


    private fun loginNow() {
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

    private fun onLoginDashboard(it: AdminTable) {
        val myApp = app as InvoiceApp
        myApp.setUser(it)
        find<AdminLoginView> {
            this.close()
        }
    }
}