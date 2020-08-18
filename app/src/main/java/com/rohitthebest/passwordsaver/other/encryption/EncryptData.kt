package com.rohitthebest.passwordsaver.other.encryption

import android.util.Base64
import android.util.Log
import java.math.BigInteger
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class EncryptData {
    companion object {
        private const val AES = "AES"
    }

    fun encryptWithSHA(data: String?): String {

        var encryptedString = ""

        try {
            val encrypt1 = SHA_Encryption()
                .encryptSHA(data?.toByteArray(), "SHA-256")
            val shaData1 = BigInteger(1, encrypt1)
            val encryptedData1 = shaData1.toString(16)

            val encrypt2 = SHA_Encryption()
                .encryptSHA(encryptedData1.toByteArray(), "SHA-384")
            val shaData2 = BigInteger(1, encrypt2)
            val encryptedData2 = shaData2.toString(16)

            val encrypt3 = SHA_Encryption()
                .encryptSHA(encryptedData2.toByteArray(), "SHA-512")
            val shaData3 = BigInteger(1, encrypt3)
            val encryptedData3 = shaData3.toString(16)

            val complexString = encryptedData1 + encryptedData2 + encryptedData3

            val finalEncrypt = SHA_Encryption()
                .encryptSHA(complexString.toByteArray(), "SHA-256")
            val finalSha = BigInteger(1, finalEncrypt)

            encryptedString = finalSha.toString(16)

        } catch (e: Exception) {
            println(e.message)
        }

        return encryptedString
    }

    @Throws(java.lang.Exception::class)
    fun encryptWithAES(data: String?, password: String?): String? {
        val key: SecretKeySpec? = generateKey(password)
        return if (key != null) {
            val cipher: Cipher = Cipher.getInstance(AES)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encVal = cipher.doFinal(data?.toByteArray())
            Base64.encodeToString(encVal, Base64.DEFAULT)
        } else {
            null
        }
    }

    @Throws(java.lang.Exception::class)
    fun decryptAES(data: String?, password: String?): String? {

        val key: SecretKeySpec? = generateKey(password)
        return if (key != null) {
            val cipher: Cipher = Cipher.getInstance(AES)
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decodedValue = Base64.decode(data, Base64.DEFAULT)
            val decValue = cipher.doFinal(decodedValue)
            String(decValue)
        } else {
            null
        }
    }


    private fun generateKey(password: String?): SecretKeySpec? {

        var secretKeySpec: SecretKeySpec? = null
        try {
            val key = SHA_Encryption().encryptSHA(password?.toByteArray(), "SHA-256")
            secretKeySpec = SecretKeySpec(key, AES)
        } catch (e: java.lang.Exception) {
            Log.i("generate Key", "Cannot Generate Key.")
        }
        return secretKeySpec
    }


}

