package com.dashlabs.invoicemanagement.databaseconnection

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.util.*

@DatabaseTable(tableName = "transactions")
class TransactionTable {
    class MeaningfulTransaction(var transactionDate: String,
                                var deduction: String)

    @DatabaseField(canBeNull = false)
    var customerId: Long = 0

    @DatabaseField(generatedId = true)
    var transactionId: Long = 0

    @DatabaseField(canBeNull = false)
    var dateCreated: Long = 0L

    @DatabaseField(canBeNull = false)
    var deduction: Double = 0.0


    fun toMeaningfulTransaction(transactionTable: TransactionTable): MeaningfulTransaction {
        return MeaningfulTransaction(Date(transactionTable.dateCreated).toString(),
                deduction.toString())
    }
}