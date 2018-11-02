package com.dashlabs.invoicemanagement.app

import com.dashlabs.invoicemanagement.databaseconnection.AdminTable
import com.dashlabs.invoicemanagement.view.admin.Admin
import com.dashlabs.invoicemanagement.view.dashboard.DashboardView
import tornadofx.*

class InvoiceApp : App(DashboardView::class) {
    var admin: Admin? = null

    fun setUser(it: AdminTable) {
        this.admin = Admin(it.name, it.password)
        println("Firing event ${it.name}")
        this.admin?.let {
            fire(AdminLoggedInEvent(it))
        }
    }

    class AdminLoggedInEvent(val admin: Admin) : FXEvent(runOn = EventBus.RunOn.ApplicationThread)
}