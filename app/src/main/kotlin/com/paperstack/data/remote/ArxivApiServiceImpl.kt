package com.paperstack.data.remote

import com.paperstack.domain.model.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val BASE_URL = "https://export.arxiv.org/api/query"
private const val RATE_LIMIT_MS = 3_000L
private const val MAX_RETRIES = 3

@Singleton
class ArxivApiServiceImpl @Inject constructor(
    private val client: OkHttpClient,
) : ArxivApiService {

    @Volatile
    private var lastRequestTimeMs: Long = 0L

    override suspend fun fetchPapers(params: FetchPapersParams): Result<FetchPapersResult> {
        enforceRateLimit()
        val url = buildUrl(params)
        val request = Request.Builder().url(url).build()
        return try {
            val result = executeWithRetry(request)
            lastRequestTimeMs = System.currentTimeMillis()
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun enforceRateLimit() {
        val elapsed = System.currentTimeMillis() - lastRequestTimeMs
        if (elapsed < RATE_LIMIT_MS) delay(RATE_LIMIT_MS - elapsed)
    }

    private suspend fun executeWithRetry(request: Request): Result<FetchPapersResult> {
        var attempt = 0
        var backoffMs = 1_000L
        while (attempt < MAX_RETRIES) {
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            when (response.code) {
                200 -> {
                    val body = response.body ?: return Result.failure(IOException("Empty body"))
                    return Result.success(parseResponse(body.byteStream()))
                }
                503 -> {
                    attempt++
                    if (attempt < MAX_RETRIES) delay(backoffMs)
                    backoffMs *= 2
                }
                else -> return Result.failure(IOException("HTTP ${response.code}"))
            }
        }
        return Result.failure(IOException("Failed after $MAX_RETRIES retries (503)"))
    }

    internal fun buildUrl(params: FetchPapersParams): String {
        val query = "cat:${params.category}"
        return "$BASE_URL?search_query=$query" +
            "&sortBy=submittedDate" +
            "&sortOrder=descending" +
            "&start=${params.start}" +
            "&max_results=${params.pageSize}"
    }

    internal fun parseResponse(stream: InputStream): FetchPapersResult {
        val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = true }
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var totalResults = 0
        var startIndex = 0
        val papers = mutableListOf<Paper>()

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "totalResults" -> totalResults = parser.nextText().trim().toIntOrNull() ?: 0
                    "startIndex"   -> startIndex = parser.nextText().trim().toIntOrNull() ?: 0
                    "entry"        -> papers.add(parseEntry(parser))
                }
            }
            event = parser.next()
        }
        return FetchPapersResult(papers = papers, totalResults = totalResults, startIndex = startIndex)
    }

    private fun parseEntry(parser: XmlPullParser): Paper {
        var id = ""; var title = ""; var abstract = ""; var submittedDate = ""
        var updatedDate = ""; var pdfUrl = ""; var primaryCategory = ""; var comment: String? = null
        val authors = mutableListOf<String>(); val categories = mutableListOf<String>()
        var inAuthor = false; var authorName = ""

        var event = parser.next()
        while (!(event == XmlPullParser.END_TAG && parser.name == "entry")) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "id"               -> id = extractPaperId(parser.nextText().trim())
                    "title"            -> title = parser.nextText().trim().replace("\n", " ")
                    "summary"          -> abstract = parser.nextText().trim().replace("\n", " ")
                    "published"        -> submittedDate = parser.nextText().trim()
                    "updated"          -> updatedDate = parser.nextText().trim()
                    "link"             -> {
                        val rel  = parser.getAttributeValue(null, "rel")
                        val type = parser.getAttributeValue(null, "type")
                        val href = parser.getAttributeValue(null, "href")
                        if (rel == "related" && type == "application/pdf" && href != null) pdfUrl = href
                    }
                    "author"           -> inAuthor = true
                    "name"             -> if (inAuthor) authorName = parser.nextText().trim()
                    "category"         -> parser.getAttributeValue(null, "term")?.let { categories.add(it) }
                    "primary_category" -> parser.getAttributeValue(null, "term")?.let { primaryCategory = it }
                    "comment"          -> comment = parser.nextText().trim().ifEmpty { null }
                }
                XmlPullParser.END_TAG -> if (parser.name == "author") {
                    if (authorName.isNotEmpty()) authors.add(authorName)
                    authorName = ""; inAuthor = false
                }
            }
            event = parser.next()
        }

        if (pdfUrl.isEmpty() && id.isNotEmpty()) pdfUrl = "https://arxiv.org/pdf/$id"

        return Paper(
            id = id, title = title, authors = authors, abstract = abstract,
            submittedDate = submittedDate, updatedDate = updatedDate, pdfUrl = pdfUrl,
            categories = categories, primaryCategory = primaryCategory, comment = comment,
        )
    }

    internal fun extractPaperId(url: String): String {
        val afterAbs = url.substringAfterLast("/abs/", missingDelimiterValue = url)
        return afterAbs.substringBefore("v")
    }
}
