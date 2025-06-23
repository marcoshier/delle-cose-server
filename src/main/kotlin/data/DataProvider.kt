package com.marcoshier.data

interface DataProvider {

    fun getDataWithHeaders(): List<Map<String, String>>?

}