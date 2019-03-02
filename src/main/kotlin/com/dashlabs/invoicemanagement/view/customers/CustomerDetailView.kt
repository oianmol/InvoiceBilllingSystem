package com.dashlabs.invoicemanagement.view.customers

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.Database
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.geometry.Insets
import tornadofx.*

class CustomerDetailView(selectedItem: CustomersTable) : View("${selectedItem.customerName} Details") {
    private var deductValue: Double = 0.0

    override val root = vbox {
        label(selectedItem.toString()) {
            vboxConstraints { margin = Insets(10.0) }
        }

        if (selectedItem.balance > 0) {
            button {
                vboxConstraints { margin = Insets(10.0) }
                text = "Pay full pending amount! ${selectedItem.balance}"
                setOnMouseClicked {
                    deductValue = selectedItem.balance
                    performBalanceReduction(selectedItem)
                }
            }

            label { text = "Or ->>" }

            hbox {
                textfield(selectedItem.balance.toString()) {
                    hboxConstraints { margin = Insets(10.0) }
                    this.filterInput {
                        it.controlNewText.isDouble() && it.controlNewText.toDouble() <= selectedItem.balance
                    }
                }.textProperty().addListener { observable, oldValue, newValue ->
                    deductValue = newValue.toDouble()
                }

                button {
                    hboxConstraints { margin = Insets(10.0) }
                    text = "Pay some pending amount from ${selectedItem.balance}"
                    setOnMouseClicked {
                        if (deductValue > 0) {
                            performBalanceReduction(selectedItem)
                        }
                    }
                }
            }
        }

    }

    private fun performBalanceReduction(selectedItem: CustomersTable) {
        Single.fromCallable {
            val customer = Database.getCustomer(selectedItem.customerId)
            customer?.let {
                it.balance = it.balance.minus(deductValue)
                Database.updateCustomer(customer)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform()).subscribe { t1, t2 ->
                    t1?.let {
                        find<CustomersView> {
                            requestForCustomers()
                            this@CustomerDetailView.close()
                        }
                    }
                    t2?.let {
                        it.message?.let { it1 -> warning(it1).show() }
                    }

                }
    }
}
