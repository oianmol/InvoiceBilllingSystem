package com.dashlabs.invoicemanagement.databaseconnection

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "products")
class ProductsTable {

    @DatabaseField(generatedId = true)
    var productId: Long = 0

    @DatabaseField(canBeNull = false)
    var productName: String = ""

    @DatabaseField(canBeNull = false)
    var dateCreated: Long = 0L

    @DatabaseField(canBeNull = false)
    var dateModified: Long = 0L

    @DatabaseField(canBeNull = false)
    var amount: Double = 0.0

    @DatabaseField(defaultValue = "false")
    var deleted: Boolean = false

    override fun toString(): String {
        return "$productName"
    }
}