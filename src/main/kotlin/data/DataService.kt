package com.marcoshier.data

import com.marcoshier.services.RefreshService
import com.marcoshier.types.Author
import com.marcoshier.types.Category
import com.marcoshier.types.Data
import com.marcoshier.types.Project
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.concurrent.thread

class DataService {

    private lateinit var provider: DataProvider

    private val googleService = GoogleSheetsService()
    private val localService = LocalService()
    private val refreshService = RefreshService(::updateData)

    private val json = Json { prettyPrint = true }

    private fun init(): Data {
        val result = googleService.init()

        result?.let {
            println("Connection to google sheets established")

            thread(isDaemon = true) {
                refreshService.tick()
            }

            provider = it
        } ?: run {
            println("Connection to google sheets failed. Using fallback local service.")

            val fallback = localService
            provider = fallback
        }

        return fetchAndSerialize()
    }

    private fun fetchAndSerialize(): Data {

        val dataMap = provider.getDataWithHeaders()


        val projectNames = mutableListOf<String>()
        var authorNames = mutableListOf<String>()
        var categoryNames = mutableListOf<String>()

        fun parseAuthorNames(query: String): List<String> {
            return query.split(", ")
        }

        fun parseCategoryNames(query: String): List<String> {
            return query.split(", ")
        }

        dataMap.forEach {
            projectNames.add(it["PROGETTI"]!!)
            authorNames.addAll(parseAuthorNames(it["AUTORI"]!!))
            categoryNames.addAll(parseCategoryNames(it["CATEGORIA"]!!))
        }

        authorNames = authorNames.distinct().toMutableList()
        categoryNames = categoryNames.distinct().toMutableList()


        val projects = dataMap.map {

            fun getPartner(): String {
                val value = it["PARTNER"]
                return if (value.isNullOrEmpty()) "" else value
            }

            fun getLocation(): String {
                val value = it["DOVE"]
                return if (value.isNullOrEmpty()) "" else value
            }

            Project(
                it["PROGETTI"]!!,
                it["ANNO"]?.toIntOrNull() ?: 9999,
                parseAuthorNames(it["AUTORI"]!!).map {
                    authorNames.indexOf(it)
                }.sorted(),
                parseCategoryNames(it["CATEGORIA"]!!).map {
                    categoryNames.indexOf(it)
                }.sorted(),
                getPartner(),
                getLocation()
            )
        }

        val authors = authorNames.mapIndexed { i, it ->
            val projectsPerAuthor = mutableListOf<Int>()

            for (p in projects) {
                if (p.authors!!.contains(i)) {
                    projectsPerAuthor.add(
                        projects.indexOf(p)
                    )
                }
            }

            Author(
                it,
                projectsPerAuthor.sorted()
            )
        }

        val categories = categoryNames.mapIndexed { i, it ->
            val projectsPerCategory = mutableListOf<Int>()

            for (p in projects) {
                if (p.categories!!.contains(i)) {
                    projectsPerCategory.add(
                        projects.indexOf(p)
                    )
                }
            }

            Category(
                it,
                projectsPerCategory.sorted()
            )
        }

        val result = Data(projects, authors, categories)

        val jsonText = json.encodeToString(Data.serializer(), result)
        val file = File("data/serialized.json")

        if (file.exists()) {
            val text = file.readText()
            if (text.hashCode() != jsonText.hashCode()) {
                file.writeText(jsonText)
            }
        } else {
            file.writeText(jsonText)
        }

        return result
    }

    fun updateData() {
        data = fetchAndSerialize()
    }



    var data = init()

}