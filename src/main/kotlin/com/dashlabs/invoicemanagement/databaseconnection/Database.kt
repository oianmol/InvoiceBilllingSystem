package com.dashlabs.invoicemanagement.databaseconnection

import com.dashlabs.invoicemanagement.view.admin.Admin
import com.dashlabs.invoicemanagement.view.admin.AdminModel
import com.dashlabs.invoicemanagement.view.customers.Customer
import com.dashlabs.invoicemanagement.view.invoices.Invoice
import com.dashlabs.invoicemanagement.view.products.Product
import com.google.gson.Gson
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.table.TableUtils
import java.io.File
import java.time.LocalDateTime
import java.time.OffsetDateTime

object Database {

    private var connectionSource: JdbcConnectionSource

    private var accountDao: Dao<AdminTable, *>?

    private var productsDao: Dao<ProductsTable, *>?

    private var customerDao: Dao<CustomersTable, *>?

    private var invoicesDao: Dao<InvoiceTable, *>?

    private var transactionsDao: Dao<TransactionTable, *>?

    init {
        connectionSource = getDatabaseConnection()

        TableUtils.createTableIfNotExists(connectionSource, AdminTable::class.java)
        TableUtils.createTableIfNotExists(connectionSource, ProductsTable::class.java)
        TableUtils.createTableIfNotExists(connectionSource, CustomersTable::class.java)
        TableUtils.createTableIfNotExists(connectionSource, InvoiceTable::class.java)
        TableUtils.createTableIfNotExists(connectionSource, TransactionTable::class.java)

        // instantiate the DAO to handle Account with String id
        accountDao = DaoManager.createDao(connectionSource, AdminTable::class.java)
        productsDao = DaoManager.createDao(connectionSource, ProductsTable::class.java)
        customerDao = DaoManager.createDao(connectionSource, CustomersTable::class.java)
        invoicesDao = DaoManager.createDao(connectionSource, InvoiceTable::class.java)
        transactionsDao = DaoManager.createDao(connectionSource, TransactionTable::class.java)

    }

    fun createAdmin(admin: Admin): AdminTable? {

        // create an instance of Account
        val adminTable = AdminTable()
        adminTable.name = admin.username
        adminTable.password = admin.password

        // persist the account object to the database
        val id = accountDao?.create(adminTable)
        connectionSource.close()

        id?.let {
            return if (it > 0) {
                adminTable
            } else {
                null
            }
        } ?: kotlin.run {
            return null
        }

    }

    fun changePassword(admin: Admin, adminModel: AdminModel): AdminTable? {
        checkAdminExists(admin)?.let {
            // persist the account object to the database
            // create an instance of Account
            val adminTable = AdminTable()
            adminTable.name = admin.username
            adminTable.password = adminModel.newpassword.value

            val id = accountDao?.update(adminTable)
            connectionSource.close()

            id?.let {
                return if (it > 0) {
                    adminTable
                } else {
                    null
                }
            } ?: kotlin.run {
                return null
            }
        } ?: kotlin.run {
            return null
        }
    }

    private fun getDatabaseConnection(): JdbcConnectionSource {
        Class.forName("org.sqlite.JDBC")
        val directory = File("~/invoicedatabase")
        directory.mkdirs()
        val dbConnectionString = "jdbc:sqlite:~/invoicedatabase/database.db"
        return JdbcConnectionSource(dbConnectionString)
    }

    fun checkAdminExists(admin: Admin): AdminTable? {
        try {
            val adminTable = AdminTable()
            adminTable.name = admin.username
            adminTable.password = admin.password
            var list = accountDao?.queryForMatchingArgs(adminTable)
            connectionSource.close()
            return list?.let {
                return if (it.size > 0) {
                    it.first()
                } else {
                    null
                }
            } ?: kotlin.run {
                return null
            }
        } catch (ex: Exception) {
            return null
        }

    }

    fun deleteProduct(productId: ProductsTable): Int? {
        return productsDao?.delete(productId)
    }

    fun createTransaction(transactionTable: TransactionTable): TransactionTable? {
        val id = transactionsDao?.create(transactionTable)
        connectionSource?.close()

        id?.let {
            return if (it > 0) {
                transactionTable
            } else {
                null
            }
        } ?: kotlin.run {
            return null
        }
    }

    fun createProduct(product: Product): ProductsTable? {
        // create an instance of product
        val productsTable = ProductsTable()
        productsTable.productName = product.name
        productsTable.dateCreated = System.currentTimeMillis()
        productsTable.dateModified = System.currentTimeMillis()
        productsTable.amount = product.amount.toDouble()
        // persist the account object to the database
        val id = productsDao?.create(productsTable)
        connectionSource.close()

        id?.let {
            return if (it > 0) {
                productsTable
            } else {
                null
            }
        } ?: kotlin.run {
            return null
        }

    }

    fun createCustomer(customer: Customer): CustomersTable? {
// create an instance of product
        val customersTable = CustomersTable()
        customersTable.customerName = customer.name
        customersTable.dateCreated = System.currentTimeMillis()
        customersTable.address = customer.address.toString()
        customersTable.dateModified = System.currentTimeMillis()
        customersTable.district = customer.district
        customersTable.state = customer.state
        // persist the account object to the database
        val id = customerDao?.create(customersTable)
        connectionSource.close()

        id?.let {
            return if (it > 0) {
                customersTable
            } else {
                null
            }
        } ?: kotlin.run {
            return null
        }
    }

    fun listProducts(search: String = ""): List<ProductsTable>? {
        return if (search.isEmpty()) {
            productsDao?.queryForAll()
        } else {
            productsDao?.queryBuilder()?.where()?.like(ProductsTable::productName.name, "%$search%")?.query()
        }
    }

    fun listCustomers(search: String = ""): List<CustomersTable>? {
        return if (search.isEmpty()) {
            customerDao?.queryForAll()
        } else {
            customerDao?.queryBuilder()?.where()?.like(CustomersTable::customerName.name, "%$search%")?.query()
        }
    }

    fun listCustomers(state: String, district: String, address: String): List<CustomersTable>? {
        return when {
            !address.isEmpty() -> customerDao?.queryBuilder()
                    ?.where()
                    ?.like(CustomersTable::state.name, state)?.and()
                    ?.like(CustomersTable::district.name, district)?.and()
                    ?.like(CustomersTable::address.name, "%$address%")
                    ?.query()
            else -> customerDao?.queryBuilder()
                    ?.where()
                    ?.like(CustomersTable::state.name, state)?.and()
                    ?.like(CustomersTable::district.name, district)
                    ?.query()
        }
    }

    fun listInvoices(): List<InvoiceTable.MeaningfulInvoice>? {
        val invoices = invoicesDao?.queryForAll()
        return invoices?.sortedByDescending { it.dateModified }?.map { it.asMeaningfulInvoice() }
    }

    fun listTransactions(customerId: Long): List<TransactionTable.MeaningfulTransaction>? {
        val invoices = transactionsDao?.queryBuilder()?.where()
                ?.like(CustomersTable::customerId.name, customerId)
                ?.query()
        return invoices?.sortedByDescending { it.dateCreated }?.map { it.toMeaningfulTransaction(it) }
    }

    fun listInvoices(customerId: Long): List<InvoiceTable.MeaningfulInvoice>? {
        val invoices = invoicesDao?.queryBuilder()?.where()?.like(CustomersTable::customerId.name, customerId)?.query()
        return invoices?.sortedByDescending { it.dateModified }?.map { it.asMeaningfulInvoice() }
    }

    fun listInvoicesSimple(customerId: Long): List<InvoiceTable>? {
        val invoices = invoicesDao?.queryBuilder()?.where()?.like(CustomersTable::customerId.name, customerId)?.query()
        return invoices?.sortedByDescending { it.dateModified }
    }

    fun listInvoices(startTime: LocalDateTime, endTime: LocalDateTime): List<InvoiceTable.MeaningfulInvoice>? {
        val invoices = invoicesDao?.queryBuilder()?.where()?.between(InvoiceTable::dateModified.name, startTime.toEpochSecond(OffsetDateTime.now().offset).times(1000), endTime.toEpochSecond(OffsetDateTime.now().offset).times(1000))?.query()
        return invoices?.sortedByDescending { it.dateModified }?.map { it.asMeaningfulInvoice() }
    }

    fun createInvoice(invoice: Invoice): InvoiceTable.MeaningfulInvoice? {
        val invoiceTable = InvoiceTable()
        invoiceTable.customerId = invoice.customerId
        invoiceTable.dateCreated = System.currentTimeMillis()
        invoiceTable.dateModified = System.currentTimeMillis()
        invoiceTable.amountTotal = invoice.productsPrice.toDouble()

        invoice.creditAmount?.let {
            if (it.toDouble() > 0) {
                invoiceTable.outstandingAmount = invoice.creditAmount.toDouble()
            }
        }

        invoiceTable.productsPurchased = Gson().toJson(invoice.productsList.map { Pair(it.productsTable, it.quantity.toInt()) }.toMutableList())
        // persist the account object to the database
        val id = invoicesDao?.create(invoiceTable)
        connectionSource.close()

        id?.let {
            return if (it > 0) {
                invoiceTable.asMeaningfulInvoice()
            } else {
                null
            }
        } ?: kotlin.run {
            return null
        }
    }

    fun getCustomer(customerId: Long): CustomersTable? {
        return customerDao?.queryBuilder()?.where()?.like(CustomersTable::customerId.name, customerId)?.query()?.first()
    }


    fun updateCustomer(customer: CustomersTable, deductValue: Double): Boolean {
        var reductionValue = deductValue

        listInvoicesSimple(customer.customerId)?.let {
            val total = it.map { it.outstandingAmount }.sum()
            if (total == deductValue) {
                it.forEach {
                    it.outstandingAmount = 0.0
                    invoicesDao?.update(it)
                }
            } else {
                val invoicesWithOut = it.filter {
                    it.outstandingAmount > 0
                }

                for (it in invoicesWithOut) {
                    if (it.outstandingAmount > reductionValue) {
                        it.outstandingAmount -= reductionValue
                        reductionValue = 0.0
                        invoicesDao?.update(it)
                        break
                    } else {
                        if (reductionValue == 0.0) {
                            break
                        }
                        // the deduction amount is greater than current invoice
                        // take the less out of reduction value
                        val deductableAmount = Math.min(it.outstandingAmount, reductionValue)
                        // deduct it from current outstanding
                        it.outstandingAmount -= deductableAmount
                        reductionValue -= deductableAmount
                        invoicesDao?.update(it)
                    }
                }

            }
        }
        return true
    }

}