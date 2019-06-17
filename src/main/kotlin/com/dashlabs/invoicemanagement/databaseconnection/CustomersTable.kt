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
    var state: String = ""

    @DatabaseField(canBeNull = false)
    var district: String = ""

    @DatabaseField(canBeNull = false)
    var dateCreated: Long = 0L

    @DatabaseField(canBeNull = false)
    var address: String = ""

    @DatabaseField(canBeNull = false)
    var dateModified: Long = 0L

    @DatabaseField(defaultValue = "false")
    var deleted: Boolean = false

    override fun toString(): String {
        return getFormattedCustomer(this)
    }

    class MeaningfulCustomer(var customerName: String, var address: String, var state: String, var district: String, var amountDue: String,var customerId: Long){
        override fun toString(): String {
            return this.let {
                val builder = StringBuilder()
                builder.append("Name: ${customerName}")
                builder.append("\n")
                builder.append("Address: ${address}")
                builder.append("\n")
                builder.append("State: ${state}")
                builder.append("\n")
                builder.append("District: ${district}")
                return builder.toString()
            }
        }
    }

    private fun getFormattedCustomer(customer: CustomersTable): String {
        customer.let {
            val builder = StringBuilder()
            builder.append("Name: ${customer.customerName}")
            builder.append("\n")
            builder.append("Address: ${customer.address}")
            builder.append("\n")
            builder.append("State: ${customer.state}")
            builder.append("\n")
            builder.append("District: ${customer.district}")
            return builder.toString()
        }
    }
}