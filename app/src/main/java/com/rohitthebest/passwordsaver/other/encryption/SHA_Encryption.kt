package com.rohitthebest.passwordsaver.other.encryption

import java.security.MessageDigest

class SHA_Encryption {

    /**
     * @param data to be encrypted
     * @param shaN encrypt method : SHA-1, SHA-224, SHA-256, SHA-384, SHA-512
     */

    @Throws(Exception::class)
    fun encryptSHA(data: ByteArray?, shaN: String?): ByteArray? {
        return if (shaN != null) {
            val sha = MessageDigest.getInstance(shaN)
            data?.let { Data ->
                sha.update(Data)
                sha.digest()
            }

        } else {
            null
        }
    }


}