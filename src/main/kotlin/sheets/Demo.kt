package com.marcoshier.sheets

import com.marcoshier.data.DataService
import com.marcoshier.data.LocalService


fun main() {

    val dataService = LocalService()
    println(dataService.getDataWithHeaders())

}
