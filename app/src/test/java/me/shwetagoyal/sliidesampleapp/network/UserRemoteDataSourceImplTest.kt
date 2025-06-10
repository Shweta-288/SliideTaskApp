package me.shwetagoyal.sliidesampleapp.network

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.content.TextContent
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import me.shwetagoyal.sliidesampleapp.domain.util.DataError
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import me.shwetagoyal.sliidesampleapp.domain.util.Result
import org.junit.jupiter.api.Test

class UserRemoteDataSourceImplTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun mockClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        HttpClient(MockEngine) {
            install(ContentNegotiation) { json(json) }
            install(DefaultRequest) {
                contentType(ContentType.Application.Json)
            }
            engine { addHandler(handler) }
        }

    @Test
    fun `getUsersLastPage returns list when header and fetch succeed`() = runTest {
        val client = mockClient { req ->
            when {
                req.method == HttpMethod.Get
                        && req.url.encodedPath == "/users"
                        && req.url.parameters["page"] == "1" -> {
                    respond(
                        "[]",
                        HttpStatusCode.OK,
                        headersOf(
                            HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()),
                            "X-Pagination-Pages" to listOf("3")
                        )
                    )
                }

                req.method == HttpMethod.Get &&
                        req.url.encodedPath == "/users" &&
                        req.url.parameters["page"] == "3" -> {
                    respond(
                        """[{"id":42,"name":"Arthur","email": "arthur@example.com","gender": "male","status": "active"}]""",
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                else -> error("Unexpected request: ${req.method} ${req.url}")
            }
        }

        val remoteDataSource = UserRemoteDataSourceImpl(client)
        val result = remoteDataSource.getUsersLastPage()

        assertTrue(result is Result.Success)
        val list = (result as Result.Success).data
        assertEquals(1, list.size)
        assertEquals(42, list[0].id)
        assertEquals("Arthur", list[0].name)
    }

    @Test
    fun `getUsersLastPage returns error when header fetch fails`() = runTest {
        val client = mockClient { req ->
            if (req.method == HttpMethod.Get
                && req.url.encodedPath == "/users"
                && req.url.parameters["page"] == "1"
            ) {
                respond(
                    content = ByteReadChannel(""),
                    status = HttpStatusCode.RequestTimeout
                )
            } else error("Unexpected ${req.method} ${req.url}")
        }

        val remoteDataSource = UserRemoteDataSourceImpl(client)
        val result = remoteDataSource.getUsersLastPage()

        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.REQUEST_TIMEOUT, (result as Result.Error).error)
    }

    @Test
    fun `addUser returns Success when 201 Created`() = runTest {
        val name = "Charlie"
        val email = "charlie@example.com"
        val createdJson =
            """{"id":7,"name":"Charlie","email":"charlie@example.com","gender":"male","status":"active"}"""

        val client = mockClient { req ->
            assertEquals(HttpMethod.Post, req.method)
            assertEquals("/users", req.url.encodedPath)

            val sent = req.body as TextContent
            assertTrue(sent.text.contains("\"name\":\"Charlie\""))
            assertTrue(sent.text.contains("\"email\":\"charlie@example.com\""))
            assertEquals(
                ContentType.Application.Json,
                sent.contentType
            )

            respond(
                createdJson,
                HttpStatusCode.Created,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val userRemoteDataSource = UserRemoteDataSourceImpl(client)
        val result = userRemoteDataSource.addUser(name, email)

        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(7, user.id)
        assertEquals("Charlie", user.name)
        assertEquals("charlie@example.com", user.email)
    }

    @Test
    fun `addUser returns Conflict error on 422 Unprocessable Entity`() = runTest {
        val client = mockClient { respond("", HttpStatusCode.UnprocessableEntity) }

        val userRemoteDataSource = UserRemoteDataSourceImpl(client)
        val result = userRemoteDataSource.addUser("Charlie", "charlie@example.com")

        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.CONFLICT, (result as Result.Error).error)
    }

    @Test
    fun `deleteUser returns Success when 204 No Content`() = runTest {
        val client = mockClient { req ->
            assertEquals(HttpMethod.Delete, req.method)
            assertEquals("/users/7", req.url.encodedPath)
            respond("", HttpStatusCode.NoContent)
        }

        val userRemoteDataSource = UserRemoteDataSourceImpl(client)
        val result = userRemoteDataSource.deleteUser(7)

        assertTrue(result is Result.Success)
        assertEquals(Unit, (result as Result.Success).data)
    }

    @Test
    fun `deleteUser returns Conflict error on 422 Unprocessable Entity`() = runTest {
        val client = mockClient { respond("", HttpStatusCode.UnprocessableEntity) }

        val userRemoteDataSource = UserRemoteDataSourceImpl(client)
        val result = userRemoteDataSource.deleteUser(7)

        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.CONFLICT, (result as Result.Error).error)
    }
}