package com.dashlabs.invoicemanagement.view.dashboard

import com.dashlabs.invoicemanagement.view.admin.Admin
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class DashboardController : Controller() {

    val statusProperty = SimpleStringProperty("No Loggedin User")
    var status by statusProperty


    val admingSettingsProperty = SimpleStringProperty("Admin Login!")
    var admingSettings by admingSettingsProperty


    fun adminLoggedin(admin: Admin) {
        runLater { status = "Welcome ${admin.username} !" }
        runLater { admingSettings = "Admin Settings" }
    }

}