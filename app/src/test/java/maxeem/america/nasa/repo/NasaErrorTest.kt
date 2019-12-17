package maxeem.america.nasa.repo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NasaErrorTest {

    private val outOfDate = """
           {"code":400,"msg":"Date must be between Jun 16, 1995 and Dec 10, 2019.","service_version":"v1"}
        """.trimIndent()
    private val internalServer = """
           {"code":500,"msg":"Internal server error","service_version":"v1"}
        """.trimIndent()
    private val apiKeyMissing = """
        {"error": {"code": "API_KEY_MISSING","message": "No api_key was supplied. Get one at https://api.nasa.gov:443"} }
    """.trimIndent()
    private val someError = """
           {"code":444,"msg":"Something went wrong","service_version":"v1"}
        """.trimIndent()
    private val badError = """
           Bad route
        """.trimIndent()
    private val blankError = """ """

    @Test
    fun outOfTheDate() {
        val info = NasaError.extract(outOfDate.trim())
        assertEquals(NasaError.Type.OutOfRange, info.type)
        assertEquals(info.text, "Date must be between Jun 16, 1995 and Dec 10, 2019.")
    }
    @Test
    fun internalServerError() {
        val info = NasaError.extract(internalServer.trim())
        assertEquals(NasaError.Type.InternalServer, info.type)
        assertTrue(info.text.equals("Internal server error", ignoreCase = true))
    }
    @Test
    fun apiKeyMissing() {
        val info = NasaError.extract(apiKeyMissing.trim())
        assertEquals(NasaError.Type.ApiKeyMissing, info.type)
        assertTrue(info.text!!.startsWith("No api_key was supplied.", ignoreCase = true))
    }
    @Test
    fun someError() {
        val info = NasaError.extract(someError.trim())
        assertEquals(info.type, NasaError.Type.Raw)
        assertTrue(info.text === someError)
    }
    @Test
    fun badError() {
        val info = NasaError.extract(badError.trim())
        assertEquals(info.type, NasaError.Type.Raw)
        assertEquals(info.text, badError)
    }
    @Test
    fun blankError() {
        val info = NasaError.extract(blankError.trim())
        assertEquals(info.type, NasaError.Type.Blank)
        assertEquals(info.text, "")
    }

}