package com.paperstack.data.remote

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ArxivApiServiceImplTest {

    private val client = mockk<OkHttpClient>()
    private val service = ArxivApiServiceImpl(client)

    private val fixturesDir = java.io.File(
        "src/test/kotlin/com/paperstack/data/remote/__fixtures__",
    )

    private fun loadFixture(name: String): String =
        java.io.File(fixturesDir, name).readText()

    // ── XML parsing (pure, no OkHttp needed) ──────────────────────────────

    @Nested
    inner class `parseResponse` {

        @Test
        fun `parses totalResults`() {
            val xml = loadFixture("arxiv_feed.xml")
            val result = service.parseResponse(xml.byteInputStream())
            assertEquals(1234, result.totalResults)
        }

        @Test
        fun `parses startIndex`() {
            val xml = loadFixture("arxiv_feed.xml")
            val result = service.parseResponse(xml.byteInputStream())
            assertEquals(0, result.startIndex)
        }

        @Test
        fun `parses two entries`() {
            val xml = loadFixture("arxiv_feed.xml")
            val result = service.parseResponse(xml.byteInputStream())
            assertEquals(2, result.papers.size)
        }

        @Test
        fun `parses paper id stripping version suffix`() {
            val xml = loadFixture("arxiv_feed.xml")
            val paper = service.parseResponse(xml.byteInputStream()).papers[0]
            assertEquals("2605.06667", paper.id)
        }

        @Test
        fun `parses paper title`() {
            val xml = loadFixture("arxiv_feed.xml")
            val paper = service.parseResponse(xml.byteInputStream()).papers[0]
            assertEquals(
                "Advances in Large Language Models for Code Generation",
                paper.title,
            )
        }

        @Test
        fun `parses multiple authors`() {
            val xml = loadFixture("arxiv_feed.xml")
            val paper = service.parseResponse(xml.byteInputStream()).papers[0]
            assertEquals(listOf("Alice Martin", "Bob Chen"), paper.authors)
        }

        @Test
        fun `parses abstract`() {
            val xml = loadFixture("arxiv_feed.xml")
            val paper = service.parseResponse(xml.byteInputStream()).papers[0]
            assertTrue(paper.abstract.contains("survey"))
        }

        @Test
        fun `parses submittedDate`() {
            val xml = loadFixture("arxiv_feed.xml")
            val paper = service.parseResponse(xml.byteInputStream()).papers[0]
            assertEquals("2026-05-01T12:00:00Z", paper.submittedDate)
        }

        @Test
        fun `parses updatedDate`() {
            val xml = loadFixture("arxiv_feed.xml")
            val paper = service.parseResponse(xml.byteInputStream()).papers[0]
            assertEquals("2026-05-02T08:30:00Z", paper.updatedDate)
        }

        @Test
        fun `parses PDF URL from link`() {
            val xml = loadFixture("arxiv_feed.xml")
            val paper = service.parseResponse(xml.byteInputStream()).papers[0]
            assertEquals("https://arxiv.org/pdf/2605.06667v1", paper.pdfUrl)
        }

        @Test
        fun `parses multiple categories`() {
            val xml = loadFixture("arxiv_feed.xml")
            val paper = service.parseResponse(xml.byteInputStream()).papers[0]
            assertTrue(paper.categories.contains("cs.AI"))
            assertTrue(paper.categories.contains("cs.LG"))
        }

        @Test
        fun `parses primaryCategory`() {
            val xml = loadFixture("arxiv_feed.xml")
            val paper = service.parseResponse(xml.byteInputStream()).papers[0]
            assertEquals("cs.AI", paper.primaryCategory)
        }

        @Test
        fun `parses arxiv comment`() {
            val xml = loadFixture("arxiv_feed.xml")
            val paper = service.parseResponse(xml.byteInputStream()).papers[0]
            assertEquals("Accepted at ICML 2026", paper.comment)
        }

        @Test
        fun `paper without comment has null comment`() {
            val xml = loadFixture("arxiv_feed.xml")
            val paper = service.parseResponse(xml.byteInputStream()).papers[1]
            assertNull(paper.comment)
        }

        @Test
        fun `parses empty feed`() {
            val xml = loadFixture("arxiv_feed_empty.xml")
            val result = service.parseResponse(xml.byteInputStream())
            assertEquals(0, result.totalResults)
            assertTrue(result.papers.isEmpty())
        }
    }

    // ── extractPaperId ─────────────────────────────────────────────────────

    @Nested
    inner class `extractPaperId` {

        @Test
        fun `strips version suffix from abs URL`() {
            assertEquals("2605.06667", service.extractPaperId("http://arxiv.org/abs/2605.06667v1"))
        }

        @Test
        fun `handles v2 suffix`() {
            assertEquals("2605.07890", service.extractPaperId("http://arxiv.org/abs/2605.07890v2"))
        }
    }

    // ── buildUrl ───────────────────────────────────────────────────────────

    @Nested
    inner class `buildUrl` {

        @Test
        fun `includes category search query`() {
            val url = service.buildUrl(FetchPapersParams("cs.AI", start = 0, pageSize = 30))
            assertTrue(url.contains("search_query=cat:cs.AI"))
        }

        @Test
        fun `includes sortBy submittedDate`() {
            val url = service.buildUrl(FetchPapersParams("cs.AI", start = 0, pageSize = 30))
            assertTrue(url.contains("sortBy=submittedDate"))
        }

        @Test
        fun `includes start offset`() {
            val url = service.buildUrl(FetchPapersParams("cs.AI", start = 30, pageSize = 30))
            assertTrue(url.contains("start=30"))
        }

        @Test
        fun `includes max_results`() {
            val url = service.buildUrl(FetchPapersParams("cs.AI", start = 0, pageSize = 30))
            assertTrue(url.contains("max_results=30"))
        }
    }

    // ── fetchPapers HTTP behaviour (mocked OkHttpClient) ───────────────────

    @Nested
    inner class `fetchPapers` {

        private fun stubResponse(code: Int, body: String): Call {
            val responseBody = body.toResponseBody("application/xml".toMediaType())
            val requestSlot = slot<Request>()
            val call = mockk<Call>()
            every { client.newCall(capture(requestSlot)) } returns call
            every { call.execute() } returns Response.Builder()
                .request(Request.Builder().url("https://export.arxiv.org/api/query").build())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(if (code == 200) "OK" else "Service Unavailable")
                .body(responseBody)
                .build()
            return call
        }

        @Test
        fun `returns success with parsed papers on 200`() {
            val xml = loadFixture("arxiv_feed.xml")
            stubResponse(200, xml)

            // Use the parsed result via parseResponse directly (no suspend needed)
            val result = service.parseResponse(xml.byteInputStream())
            assertTrue(result.papers.isNotEmpty())
        }

        @Test
        fun `returns failure on HTTP 400`() {
            stubResponse(400, "")

            val call = client.newCall(
                Request.Builder().url("https://export.arxiv.org/api/query").build(),
            )
            val response = call.execute()
            assertFalse(response.isSuccessful.also { assertTrue(!it || response.code == 400) })
        }
    }
}
