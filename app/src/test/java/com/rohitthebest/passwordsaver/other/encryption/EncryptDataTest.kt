package com.rohitthebest.passwordsaver.other.encryption

import android.util.Base64
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.util.*


class EncryptDataTest {

    private lateinit var encryptData: EncryptData
    private var password = ""
    private var data = ""

    @Before
    fun setUp() {

        encryptData = EncryptData()
        password = UUID.randomUUID().toString()
        data = "This is some test data which needs to be encrypted and decrypted"


        // mocking the android.util.Base64 encodeToString and decode functions
        mockkStatic(android.util.Base64::class)
        val arraySlot = slot<ByteArray>()

        every {
            Base64.encodeToString(capture(arraySlot), Base64.DEFAULT)
        } answers {
            java.util.Base64.getEncoder().encodeToString(arraySlot.captured)
        }

        val stringSlot = slot<String>()
        every {
            Base64.decode(capture(stringSlot), Base64.DEFAULT)
        } answers {
            java.util.Base64.getDecoder().decode(stringSlot.captured)
        }
    }

    @Test
    fun encryptWithSHA() {

        val encryptedPassword = encryptData.encryptWithSHA(password)

        assertNotEquals(password, encryptedPassword)

    }

    @Test
    fun `data is encrypted using the password`() {

        val encryptedData = encryptData.encryptWithAES(data, password)

        assertNotEquals(encryptedData, data)
    }

    @Test
    fun `data is decrypted using the same password`() {

        val encryptedData = encryptData.encryptWithAES(data, password)

        val decryptedPassword = encryptData.decryptAES(encryptedData, password)

        assertEquals(data, decryptedPassword)
    }
}