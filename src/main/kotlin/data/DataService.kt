package com.marcoshier.data

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.marcoshier.lib.findMatch
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

        if (!dataMap.isEmpty() && !dataMap[0].isEmpty()) {
            val deserializedFile = File("data/unserialized.csv")

            if (!deserializedFile.exists()) {
                deserializedFile.createNewFile()
            }

            csvWriter().open(deserializedFile) {
                val headers = dataMap.first().keys.toList()
                writeRow(headers)

                dataMap.forEach { row ->
                    val values = headers.map { header -> row[header] ?: "" }
                    writeRow(values)
                }
            }
        }


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
        val serializedFile = File("data/serialized.json")

        if (serializedFile.exists()) {
            val text = serializedFile.readText()
            if (text.hashCode() != jsonText.hashCode()) {
                serializedFile.writeText(jsonText)
            }
        } else {
            serializedFile.writeText(jsonText)
        }

        return result
    }

    fun updateData() {
        data = fetchAndSerialize()
    }



    var data = init()


    /** Getters */

    val projects: List<String>
        get() = data.projects.map { it.name }

    val authors: List<String>
        get() = data.authors.map { it.name }

    val categories: List<String>
        get() = data.categories.map { it.name }


    fun getProject(name: String): Project? {
        val p = findMatch(projects, name)
        return data.projects.find { it.name == p }
    }

    fun getAuthor(name: String): Author? {
        val a = findMatch(authors, name)
        return data.authors.find { it.name == a }
    }

}