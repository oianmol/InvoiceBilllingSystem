package com.dashlabs.invoicemanagement.databaseconnection

import com.google.gson.annotations.SerializedName
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "invoices")
class InvoiceTable {

    @DatabaseField(canBeNull = false)
    var customerId: Long = 0

    @DatabaseField(generatedId = true)
    var invoiceId: Long = 0

    @DatabaseField(canBeNull = false)
    var dateCreated: Long = 0L

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    var productsPurchased: String = ""

    @DatabaseField(canBeNull = false)
    var dateModified: Long = 0L

    @DatabaseField(canBeNull = false)
    var amountTotal: Double = 0.0

    @DatabaseField(canBeNull = false)
    var outstandingAmount: Double = 0.0

    override fun toString(): String {
        return "$customerId $invoiceId $productsPurchased $dateCreated $dateModified"
    }
}