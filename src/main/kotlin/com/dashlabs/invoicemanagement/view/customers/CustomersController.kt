package com.dashlabs.invoicemanagement.view.customers

import com.dashlabs.invoicemanagement.databaseconnection.CustomersTable
import com.dashlabs.invoicemanagement.databaseconnection.Database
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.Property
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import tornadofx.*

class CustomersController : Controller() {

    val customersListObserver = SimpleListProperty<CustomersTable>()

    fun requestForCustomers() {
        val listOfCustomers = Database.listCustomers()
        runLater {
            listOfCustomers?.let {
                customersListObserver.set(FXCollections.observableArrayList(it))
            }
        }
    }

    fun searchProduct(username: Property<String>) {
        Single.create<List<CustomersTable>> {
            try {
                val listOfCustomers = Database.listCustomers(search = username.value)
                listOfCustomers?.let { it1 -> it.onSuccess(it1) }
            } catch (ex: Exception) {
                it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe { t1, t2 ->
                    t1?.let {
                        it.isNotEmpty().let {
                            if (it) {
                                username.value = ""
                            }
                        }
                        customersListObserver.set(FXCollections.observableArrayList<CustomersTable>(it))
                    }
                }
    }

    fun addCustomer(customerName: Property<String>, address: Property<String>, state: Property<String>, district: Property<String>) {
        Single.create<CustomersTable> {
            try {
                if (customerName.value.isNullOrEmpty() || address.value.isNullOrEmpty()|| state.value.isNullOrEmpty() || district.value.isNullOrEmpty()) {
                    it.onError(Exception())
                } else {
                    val customer = Customer()
                    customer.name = customerName.value
                    customer.address = address.value
                    customer.state = state.value
                    customer.district  = district.value
                    Database.createCustomer(customer)?.let { it1 -> it.onSuccess(it1) }
                }
            } catch (ex: Exception) {
                it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe { t1, t2 ->
                    t1?.let {
                        customersListObserver.add(it)
                        print(it)
                    }
                    t2?.let {
                        print(it)
                        it.message?.let { it1 -> warning(it1).show() }
                    }
                }
    }

}

class CustomerViewModel : ItemViewModel<Customer>(Customer()) {
    val customerName = bind(Customer::nameProperty)
    val searchName = bind(Customer::searchProperty)
    val address = bind(Customer::address)
    val state = bind(Customer::state)
    val district = bind(Customer::districtProperty)
}