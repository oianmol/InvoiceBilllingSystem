package com.dashlabs.invoicemanagement.view.invoices

import com.dashlabs.WebView
import com.dashlabs.WebViewFactory
import com.dashlabs.invoicemanagement.InvoiceGenerator
import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.InvoiceTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.geometry.Insets
import tornadofx.*
import java.io.File
import java.util.*


class InvoiceDetailView(selectedItem: InvoiceTable, customer: CustomersTable) : View("Invoice Details") {

    val list = Gson().fromJson<ArrayList<Pair<ProductsTable, Int>>>(selectedItem.productsPurchased, object : TypeToken<ArrayList<Pair<ProductsTable, Int>>>() {}.type)

    override val root = vbox {

        label(text = customer.toString()) {
            vboxConstraints { margin = Insets(10.0) }
        }

        label(text = "Last Modified on ${Date(selectedItem.dateModified)}") {
            vboxConstraints { margin = Insets(10.0) }
        }

        label(text = "Total Amount: ${list.map { it.first.amount.times(it.second) }.sum()}") {
            vboxConstraints { margin = Insets(10.0) }
        }

        Single.fromCallable {
            val file = File("~/invoicedatabase", "temp.pdf")
            file.delete()
            file.createNewFile()
            InvoiceGenerator.makePDF(file, selectedItem, list.map { Pair(it.first, it.second) }.toMutableList())
            file
        }.subscribeOn(Schedulers.io()).observeOn(JavaFxScheduler.platform()).subscribe { t1, t2 ->
            val webView = WebViewFactory.getWebView()
            webView.load("file://$t1.absolutePath")
            this@vbox.add(webView.node)
        }

    }
}


class JSLogListener {

    fun log(text: String) {
        println(text)
    }
}