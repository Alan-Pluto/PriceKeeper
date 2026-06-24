package com.pricekeeper.app.core.location

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ServerSocket
import kotlin.concurrent.thread

class MapShareLocationParserTest {

    @Test
    fun `parseStoreCoordinates parses comma separated coordinates`() {
        assertEquals(31.2304 to 121.4737, parseStoreCoordinates("31.2304,121.4737"))
    }

    @Test
    fun `parseStoreCoordinates parses longitude latitude comma coordinates`() {
        assertEquals(31.2304 to 121.4737, parseStoreCoordinates("121.4737,31.2304"))
    }

    @Test
    fun `parseStoreCoordinates parses lat lng query parameters`() {
        assertEquals(
            31.2304 to 121.4737,
            parseStoreCoordinates("https://maps.example/place?lat=31.2304&lng=121.4737")
        )
    }

    @Test
    fun `parseStoreCoordinates parses lng lat query parameters`() {
        assertEquals(
            31.2304 to 121.4737,
            parseStoreCoordinates("https://maps.example/place?longitude=121.4737&latitude=31.2304")
        )
    }

    @Test
    fun `parseStoreCoordinates parses amap poi p parameter`() {
        assertEquals(
            31.424638348061258 to 118.3876472711563,
            parseStoreCoordinates(
                "https://www.amap.com/?p=B0LDOL5KVV,31.424638348061258,118.3876472711563,芜湖经开区希尔顿花园酒店,环湖西路凤鸣研创中心10号楼"
            )
        )
    }

    @Test
    fun `normalizeMapUrl keeps http links as fallback navigation target`() {
        assertEquals(
            "https://surl.amap.com/bGKm9Ho1b2us",
            normalizeMapUrl(" https://surl.amap.com/bGKm9Ho1b2us ")
        )
    }

    @Test
    fun `detectMapShareLinkType identifies amap short link`() {
        assertEquals(MapShareLinkType.Short, detectMapShareLinkType("https://surl.amap.com/bGKm9Ho1b2us"))
    }

    @Test
    fun `detectMapShareLinkType treats normal map url as long link`() {
        assertEquals(
            MapShareLinkType.Long,
            detectMapShareLinkType(
                "https://www.amap.com/?p=B0LDOL5KVV,31.424638348061258,118.3876472711563,hotel,address"
            )
        )
    }

    @Test
    fun `expandMapShareUrl follows redirects to final map link`() = runTest {
        val server = ServerSocket(0)
        val port = server.localPort
        val finalUrl =
            "http://127.0.0.1:$port/final?p=B0LDOL5KVV,31.424638348061258,118.3876472711563,hotel,address"
        val serverThread = thread(start = true) {
            repeat(2) {
                val socket = server.accept()
                val requestLine = socket.getInputStream().bufferedReader().readLine().orEmpty()
                val response = if (requestLine.contains("/short")) {
                    "HTTP/1.1 302 Found\r\nLocation: $finalUrl\r\nContent-Length: 0\r\n\r\n"
                } else {
                    "HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n"
                }
                socket.getOutputStream().write(response.toByteArray())
                socket.close()
            }
        }
        try {
            assertEquals(finalUrl, expandMapShareUrl("http://127.0.0.1:$port/short"))
        } finally {
            server.close()
            serverThread.join(1_000)
        }
    }

    @Test
    fun `expandMapShareUrl stops at first redirect url containing coordinates`() = runTest {
        val server = ServerSocket(0)
        val port = server.localPort
        val coordinateUrl =
            "http://127.0.0.1:$port/poi?p=B0LDOL5KVV,31.424638348061258,118.3876472711563,hotel,address"
        val callAppUrl = "http://127.0.0.1:$port/callAPP"
        val serverThread = thread(start = true) {
            repeat(2) {
                val socket = server.accept()
                val requestLine = socket.getInputStream().bufferedReader().readLine().orEmpty()
                val response = when {
                    requestLine.contains("/short") ->
                        "HTTP/1.1 302 Found\r\nLocation: $coordinateUrl\r\nContent-Length: 0\r\n\r\n"
                    requestLine.contains("/poi") ->
                        "HTTP/1.1 302 Found\r\nLocation: $callAppUrl\r\nContent-Length: 0\r\n\r\n"
                    else ->
                        "HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n"
                }
                socket.getOutputStream().write(response.toByteArray())
                socket.close()
            }
        }
        try {
            assertEquals(coordinateUrl, expandMapShareUrl("http://127.0.0.1:$port/short"))
        } finally {
            server.close()
            serverThread.join(1_000)
        }
    }

    @Test
    fun `parseStoreCoordinates rejects out of range values`() {
        assertNull(parseStoreCoordinates("121.4737,999.0"))
    }

    @Test
    fun `parseMapShareLocation expands short link and saves expanded map url when parsed`() = runTest {
        val shortUrl = "https://surl.amap.com/bGKm9Ho1b2us"
        val expandedUrl =
            "https://www.amap.com/?p=B0LDOL5KVV,31.424638348061258,118.3876472711563,hotel,address"

        val result = parseMapShareLocation(shortUrl) { ExpandedMapShare(expandedUrl) }

        assertTrue(result is StoreLocationParseResult.Parsed)
        result as StoreLocationParseResult.Parsed
        assertEquals(31.424638348061258, result.latitude, 0.0)
        assertEquals(118.3876472711563, result.longitude, 0.0)
        assertEquals(expandedUrl, result.mapUrl)
    }

    @Test
    fun `parseMapShareLocation parses long link without expanding`() = runTest {
        val longUrl = "https://maps.example/place?lat=31.2304&lng=121.4737"
        var expandCalled = false

        val result = parseMapShareLocation(longUrl) {
            expandCalled = true
            null
        }

        assertTrue(result is StoreLocationParseResult.Parsed)
        result as StoreLocationParseResult.Parsed
        assertEquals(31.2304, result.latitude, 0.0)
        assertEquals(121.4737, result.longitude, 0.0)
        assertEquals(longUrl, result.mapUrl)
        assertEquals(false, expandCalled)
    }

    @Test
    fun `parseMapShareLocation saves original short link when expansion cannot be parsed`() = runTest {
        val shortUrl = "https://surl.amap.com/bGKm9Ho1b2us"

        val result = parseMapShareLocation(shortUrl) { ExpandedMapShare("https://www.amap.com/place-without-coordinates") }

        assertEquals(StoreLocationParseResult.LinkOnly(shortUrl), result)
    }

    @Test
    fun `parseMapShareLocation uses expanded page body as fallback`() = runTest {
        val shortUrl = "https://surl.amap.com/bGKm9Ho1b2us"

        val result = parseMapShareLocation(shortUrl) {
            ExpandedMapShare(
                url = "https://www.amap.com/place-without-coordinates",
                bodySnippet = """{"lat":31.424638348061258,"lng":118.3876472711563}"""
            )
        }

        assertTrue(result is StoreLocationParseResult.Parsed)
        result as StoreLocationParseResult.Parsed
        assertEquals(31.424638348061258, result.latitude, 0.0)
        assertEquals(118.3876472711563, result.longitude, 0.0)
        assertEquals("https://www.amap.com/place-without-coordinates", result.mapUrl)
    }

    @Test
    fun `parseMapShareLocation rejects non link input`() = runTest {
        val result = parseMapShareLocation("31.2304,121.4737")

        assertTrue(result is StoreLocationParseResult.Invalid)
    }
}
