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
    var sectionName: String = ""

    override fun toString(): String {
        return "$productId $productName $sectionName"
    }
}