package com.marcoshier.data

import com.marcoshier.sheets.GoogleSheetsClient

class GoogleSheetsService: DataProvider {

    val spreadsheetId = "1PNeH6mq9p8jfxFKOBWUN234xOeYohi5gzGW-3XjNw_E"
    var client: GoogleSheetsClient? = null

    fun init(): GoogleSheetsService? {
        try {
            client = GoogleSheetsClient()
            client?.readSheet(spreadsheetId)
            return this
        } catch(e: Throwable) {
            e.printStackTrace()
            return null
        }
    }

    override fun getDataWithHeaders(): List<Map<String, String>>? {
        return client!!.readSheetAsRecords(spreadsheetId, "A1:F54")
    }

}