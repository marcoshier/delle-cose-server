package com.marcoshier.data

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File

class LocalService: DataProvider {

    val file = File("data/14-06-2025.csv")

    override fun getDataWithHeaders(): List<Map<String, String>> {
        return csvReader().readAllWithHeader(file)
    }

}