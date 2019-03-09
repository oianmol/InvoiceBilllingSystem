package com.dashlabs.invoicemanagement.view.admin

import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*

class ChangePasswordView(val admin: Admin) : View("ChangePassword") {

    private val adminModel = AdminModel()


    override val root = form {
        fieldset(title, labelPosition = Orientation.VERTICAL) {
            field("Current Password") {
                textfield(adminModel.username).validator {
                    if (it.isNullOrBlank()) error("The current password field is required") else null
                }
            }
            field("New Password") {
                passwordfield(adminModel.password).validator {
                    if (it.isNullOrBlank()) error("The password field is required") else null
                }
            }

            field("Confirm New Password") {
                passwordfield(adminModel.newpassword).validator {
                    if (it.isNullOrBlank()) error("The Confirm new password field is required") else null
                }
            }

            button("Change Password!") {
                hboxConstraints {
                    marginRight = 20.0
                    hGrow = Priority.ALWAYS
                }
                action {
                    if (adminModel.password.value.equals(adminModel.newpassword.value)) {
                        changePassword()
                    } else {
                        information("New Passwords don't match")
                    }
                }
            }
        }

    }

    private fun changePassword() {
        adminModel.changePassword(admin, adminModel)
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe { t1, t2 ->
                    t1?.let {
                        information("Password Changed ${it.name}! Start again!")
                        System.exit(0)
                    }
                    t2?.let {
                        it.message?.let { it1 -> information(it1) }
                    }
                }
    }
}