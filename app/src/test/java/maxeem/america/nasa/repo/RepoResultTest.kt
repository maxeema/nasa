package maxeem.america.nasa.repo

import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URI

class RepoResultTest {

    @Test
    fun failures() {
        var err1 = RepoResult.Failure.Api("Bad exception")
        assertSame("Bad exception", err1.msg)

        err1 = RepoResult.Failure.Api("Bad exception", err = IllegalStateException())
        assertTrue(err1.cause is java.lang.IllegalStateException)
        assertTrue(err1.msg == "Bad exception")
        assertTrue(err1.message == "Bad exception")

        val err2 = RepoResult.Failure.Lang(ArrayIndexOutOfBoundsException(-1))
        assertTrue(err2.err is ArrayIndexOutOfBoundsException)
        assertTrue(err2.cause is ArrayIndexOutOfBoundsException)

        val err3 = RepoResult.Failure.Network(SocketTimeoutException())
        assertTrue(err3.err is IOException)
        assertTrue(err3.cause is IOException)
    }
    @Test
    fun good() {
        val good = RepoResult successOf URI.create("http://some.url")
        assertTrue(good.isGood)
        assertFalse(good.isBad)
        assertTrue(good.good is URI)
    }
    @Test
    fun bad() {
        val bad = RepoResult.of<String>(RepoResult.Failure.Lang(OutOfMemoryError("No more memory")))
        assertTrue(bad.isBad)
        assertFalse(bad.isGood)
    }

}