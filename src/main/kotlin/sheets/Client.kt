package com.marcoshier.sheets

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.File
import java.io.FileReader
import java.io.IOException

class GoogleSheetsClient() {

    private val sheetsService: Sheets = init()


    private fun authorize(): Credential {
        val clientSecrets = GoogleClientSecrets.load(
            GsonFactory.getDefaultInstance(),
            FileReader("data/credentials.json")
        )

        val scopes = listOf(SheetsScopes.SPREADSHEETS_READONLY)

        val authFlow = GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), clientSecrets, scopes)
            .setDataStoreFactory(FileDataStoreFactory(File("data")))
            .setAccessType("offline")
            .build()

        return AuthorizationCodeInstalledApp(authFlow, LocalServerReceiver()).authorize("user")

    }

    private fun init(): Sheets {
        return Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), authorize())
            .setApplicationName("delle cose - API")
            .build()
    }

    /**
     * Reads values from a specific range in a Google Sheet
     */
    fun readSheet(spreadsheetId: String, range: String): List<List<Any>>? {
        return try {
            val response: ValueRange = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()

            response.getValues()
        } catch (e: IOException) {
            println("Error reading sheet: ${e.message}")
            null
        }
    }


    fun readSheet(spreadsheetId: String): List<List<Any>>? {
        return readSheet(spreadsheetId, "A:Z")
    }


    fun readSheetAsRecords(spreadsheetId: String, range: String): List<Map<String, String>>? {
        val values = readSheet(spreadsheetId, range) ?: return null

        if (values.isEmpty()) return emptyList()

        val headers = values[0].map { it.toString() }
        val records = mutableListOf<Map<String, String>>()

        for (i in 1 until values.size) {
            val row = values[i]
            val record = mutableMapOf<String, String>()

            headers.forEachIndexed { index, header ->
                val cellValue = if (index < row.size) row[index].toString() else ""
                record[header] = cellValue
            }

            records.add(record)
        }

        return records
    }
}

