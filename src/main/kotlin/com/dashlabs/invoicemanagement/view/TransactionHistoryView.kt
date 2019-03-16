package com.dashlabs.invoicemanagement.view

import com.dashlabs.invoicemanagement.databaseconnection.TransactionTable
import com.dashlabs.invoicemanagement.view.invoices.InvoicesController
import javafx.geometry.Insets
import tornadofx.*

class TransactionHistoryView(customerId: Long) : View("Transaction history") {

    private val invoicesController: InvoicesController by inject()

    override val root = vbox {
        invoicesController.getTransactionHistory(customerId)
        minWidth = 600.0
        minHeight = 600.0
        tableview<TransactionTable.MeaningfulTransaction>(invoicesController.transactionListObserver) {
            vboxConstraints { margin = Insets(20.0) }
            stylesheets.add("jfx-table-view.css")
            column("Transaction Date", TransactionTable.MeaningfulTransaction::transactionDate)
            column("Received Amount", TransactionTable.MeaningfulTransaction::deduction)
        }
    }
}