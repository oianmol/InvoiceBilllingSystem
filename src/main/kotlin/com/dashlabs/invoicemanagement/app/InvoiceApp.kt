package com.dashlabs.invoicemanagement.app

import com.dashlabs.invoicemanagement.databaseconnection.AdminTable
import com.dashlabs.invoicemanagement.databaseconnection.InvoiceTable
import com.dashlabs.invoicemanagement.view.admin.Admin
import com.dashlabs.invoicemanagement.view.dashboard.DashboardView
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.nio.file.Files

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

fun savePdf(it: InvoiceTable.MeaningfulInvoice, file: File) {
    val fileChooser = FileChooser()
    fileChooser.initialFileName = "${it.customerId}-${it.invoiceId}-${it.dateModified}.pdf"
    val extFilter = FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf")
    fileChooser.extensionFilters.add(extFilter)
    fileChooser.title = "Save Invoice"
    val dest = fileChooser.showSaveDialog(null)
    if (dest != null) {
        try {
            Files.copy(file.toPath(), dest.toPath())
        } catch (ex: Exception) {

        }

    }
}