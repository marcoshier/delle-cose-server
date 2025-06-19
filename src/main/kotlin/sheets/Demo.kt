package com.marcoshier.sheets

import com.marcoshier.data.DataService


// Example usage
fun main() {


    val dataService = DataService()
    println(dataService.data)


   /* // Example 1: Read all data from first sheet
    println("=== Reading First Sheet ===")
    val allData = client.readFirstSheet(spreadsheetId)
    allData?.forEach { row ->
        println(row.joinToString(" | "))
    }

    // Example 2: Read specific range
    println("\n=== Reading Specific Range (A1:C10) ===")
    val rangeData = client.readSheet(spreadsheetId, "A1:C10")
    rangeData?.forEach { row ->
        println(row.joinToString(" | "))
    }

    // Example 3: Read sheet by name
    println("\n=== Reading Sheet by Name ===")
    val namedSheetData = client.readSheetByName(spreadsheetId, "Sheet1", "A1:D5")
    namedSheetData?.forEach { row ->
        println(row.joinToString(" | "))
    }

    // Example 4: Get sheet metadata
    println("\n=== Sheet Metadata ===")
    val metadata = client.getSheetMetadata(spreadsheetId)
    metadata?.let { meta ->
        println("Spreadsheet Title: ${meta["title"]}")
        val sheets = meta["sheets"] as List<*>
        sheets.forEach { sheet ->
            val sheetMap = sheet as Map<*, *>
            println("Sheet: ${sheetMap["title"]} (ID: ${sheetMap["sheetId"]})")
        }
    }

    // Example 5: Read as structured records
    println("\n=== Reading as Structured Records ===")
    val records = client.readSheetAsRecords(spreadsheetId, "A1:D10")
    records?.forEach { record ->
        println(record)
    }*/
}
