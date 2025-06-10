package me.shwetagoyal.sliidesampleapp.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals

import me.shwetagoyal.sliidesampleapp.domain.util.DataError
import me.shwetagoyal.sliidesampleapp.domain.util.Result
import org.junit.jupiter.api.Test

@Serializable
private data class TestUser(val id: Int, val name: String)

class HttpClientExtTest {

    private val jsonConfig = Json { ignoreUnknownKeys = true }

    private fun mockClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ) = HttpClient(MockEngine) {
        install(ContentNegotiation) { json(jsonConfig) }
        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
        }
        engine { addHandler(handler) }
    }

    @Test
    fun `GET returns Success when 200`() = runTest {
        val client = mockClient { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/users/1", request.url.encodedPath)
            respond(
                """{"id":1,"name":"Alice"}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val result = client.get<TestUser>("users/1")
        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.id)
        assertEquals("Alice", result.data.name)
    }

    @Test
    fun `GET returns NotFound when 404`() = runTest {
        val client = mockClient { _ ->
            respond("", HttpStatusCode.NotFound)
        }

        val result = client.get<TestUser>("users/1")
        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.UNKNOWN, (result as Result.Error).error)
    }

    @Test
    fun `POST returns Success when 201`() = runTest {
        val newUser = TestUser(2, "Bob")
        val client = mockClient { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/users", request.url.encodedPath)

            val sent = request.body as TextContent
            assertTrue(sent.text.contains("\"id\":2"))
            assertTrue(sent.text.contains("\"name\":\"Bob\""))
            assertEquals(
                ContentType.Application.Json,
                sent.contentType
            )

            respond(
                """{"id":2,"name":"Bob"}""",
                HttpStatusCode.Created,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val result = client.post<TestUser, TestUser>("users", newUser)
        assertTrue(result is Result.Success)
        val returned = (result as Result.Success).data
        assertEquals(2, returned.id)
        assertEquals("Bob", returned.name)
    }

    @Test
    fun `DELETE returns Success for no content and Unit for Unit`() = runTest {
        val client = mockClient { request ->
            assertEquals(HttpMethod.Delete, request.method)
            assertEquals("/users/7", request.url.encodedPath)
            respond("", HttpStatusCode.NoContent)
        }

        val result = client.delete<Unit>("users/7")
        assertTrue(result is Result.Success)
        assertEquals(Unit, (result as Result.Success).data)
    }

    @Test
    fun `DELETE returns Conflict error on 422`() = runTest {
        val client = mockClient { _ ->
            respond("", HttpStatusCode.UnprocessableEntity)
        }

        val result = client.delete<Unit>("users/7")
        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.CONFLICT, (result as Result.Error).error)
    }
}