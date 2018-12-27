package com.dashlabs.invoicemanagement.databaseconnection

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "customers")
class CustomersTable {

    @DatabaseField(generatedId = true)
    var customerId: Long = 0

    @DatabaseField(canBeNull = false)
    var customerName: String = ""

    @DatabaseField(canBeNull = false)
    var dateCreated: Long = 0L

    @DatabaseField(canBeNull = false)
    var aadharCard : String = ""

    @DatabaseField(canBeNull = false)
    var dateModified: Long = 0L

    @DatabaseField(canBeNull = false)
    var balance: Double = 0.0

    override fun toString(): String {
        return "$customerId $customerName $balance $dateCreated $dateModified"
    }
}