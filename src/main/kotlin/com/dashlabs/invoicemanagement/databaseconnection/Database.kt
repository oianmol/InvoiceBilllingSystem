package com.dashlabs.invoicemanagement.databaseconnection

import com.dashlabs.invoicemanagement.view.admin.Admin
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.table.TableUtils
import java.io.File


object Database {

    private var connectionSource: JdbcConnectionSource

    private var accountDao: Dao<AdminTable, *>?

    init {
        connectionSource = getDatabaseConnection()
        // instantiate the DAO to handle Account with String id
        accountDao = DaoManager.createDao(connectionSource, AdminTable::class.java)
    }

    fun createAdmin(admin: Admin): AdminTable? {
        TableUtils.createTableIfNotExists(connectionSource, AdminTable::class.java)

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

    private fun getDatabaseConnection(): JdbcConnectionSource {
        Class.forName("org.sqlite.JDBC")
        val directory = File("~/invoicedatabase")
        directory.mkdirs()
        val dbConnectionString = "jdbc:sqlite:~/invoicedatabase/database.db"
        return JdbcConnectionSource(dbConnectionString)
    }

    fun checkAdminExists(admin: Admin): AdminTable? {
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
    }

}