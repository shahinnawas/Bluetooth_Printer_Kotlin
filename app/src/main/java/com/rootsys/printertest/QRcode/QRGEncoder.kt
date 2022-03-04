package com.rootsys.printertest.QRcode

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*

class QRGEncoder {
    private var WHITE = -0x1
    private var BLACK = -0x1000000
    private var dimension = Int.MIN_VALUE
    private var contents: String? = null
    private var displayContents: String? = null
    private var title: String? = null
    private var format: BarcodeFormat? = null
    private var encoded = false

    fun setColorWhite(color: Int) {
        WHITE = color
    }

    fun setColorBlack(color: Int) {
        BLACK = color
    }

    fun getColorWhite(): Int {
        return WHITE
    }

    fun getColorBlack(): Int {
        return BLACK
    }

    fun QRGEncoder(data: String?, type: String?) {
        encoded = encodeContents(data, null, QRGContents.Type.TEXT)
    }

    fun QRGEncoder(data: String?, type: String?, dimension: Int) {
        this.dimension = dimension
        encoded = encodeContents(data, null, QRGContents.Type.TEXT)
    }

    fun QRGEncoders(data: String?, bundle: Bundle?, type: String, dimension: Int) {
        this.dimension = dimension
        encoded = encodeContents(data, bundle, type)
    }

    fun getTitle(): String? {
        return title
    }

    private fun encodeContents(data: String?, bundle: Bundle?, type: String): Boolean {
        // Default to QR_CODE if no format given.
        format = BarcodeFormat.QR_CODE
        if (format == BarcodeFormat.QR_CODE) {
            format = BarcodeFormat.QR_CODE
            encodeQRCodeContents(data, bundle, type)
        } else if (data != null && data.length > 0) {
            contents = data
            displayContents = data
            title = "Text"
        }
        return contents != null && contents!!.length > 0
    }

    private fun encodeQRCodeContents(data: String?, bundle: Bundle?, type: String) {
        var data = data
        when (type) {
            QRGContents.Type.TEXT -> if (data != null && data.length > 0) {
                contents = data
                displayContents = data
                title = "Text"
            }
            QRGContents.Type.EMAIL -> {
                data = trim(data)
                if (data != null) {
                    contents = "mailto:$data"
                    displayContents = data
                    title = "E-Mail"
                }
            }
            QRGContents.Type.PHONE -> {
                data = trim(data)
                if (data != null) {
                    contents = "tel:$data"
                    displayContents = PhoneNumberUtils.formatNumber(data)
                    title = "Phone"
                }
            }
            QRGContents.Type.SMS -> {
                data = trim(data)
                if (data != null) {
                    contents = "sms:$data"
                    displayContents = PhoneNumberUtils.formatNumber(data)
                    title = "SMS"
                }
            }
            QRGContents.Type.CONTACT -> if (bundle != null) {
                val newContents = StringBuilder(100)
                val newDisplayContents = StringBuilder(100)
                newContents.append("VCARD:")
                val name = trim(bundle.getString(ContactsContract.Intents.Insert.NAME))
                if (name != null) {
                    newContents.append("N:").append(escapeVCard(name)).append(';')
                    newDisplayContents.append(name)
                }
                val address = trim(bundle.getString(ContactsContract.Intents.Insert.POSTAL))
                if (address != null) {
                    newContents.append("ADR:").append(escapeVCard(address)).append(';')
                    newDisplayContents.append('\n').append(address)
                }
                val uniquePhones: MutableCollection<String> =
                    HashSet<String>(PHONE_KEYS.size)
                run {
                    var x = 0
                    while (x < PHONE_KEYS.size) {
                        val phone =
                            trim(bundle.getString(PHONE_KEYS.get(x)))
                        if (phone != null) {
                            uniquePhones.add(phone)
                        }
                        x++
                    }
                }
                for (phone in uniquePhones) {
                    newContents.append("TEL:").append(escapeVCard(phone)).append(';')
                    newDisplayContents.append('\n').append(PhoneNumberUtils.formatNumber(phone))
                }
                val uniqueEmails: MutableCollection<String> =
                    HashSet<String>(EMAIL_KEYS.size)
                var x = 0
                while (x < EMAIL_KEYS.size) {
                    val email = trim(bundle.getString(EMAIL_KEYS.get(x)))
                    if (email != null) {
                        uniqueEmails.add(email)
                    }
                    x++
                }
                for (email in uniqueEmails) {
                    newContents.append("EMAIL:").append(escapeVCard(email)).append(';')
                    newDisplayContents.append('\n').append(email)
                }
                val url = trim(bundle.getString(URL_KEY))
                if (url != null) {
                    // escapeVCard(url) -> wrong escape e.g. http\://zxing.google.com
                    newContents.append("URL:").append(url).append(';')
                    newDisplayContents.append('\n').append(url)
                }
                val note = trim(bundle.getString(NOTE_KEY))
                if (note != null) {
                    newContents.append("NOTE:").append(escapeVCard(note)).append(';')
                    newDisplayContents.append('\n').append(note)
                }

                // Make sure we've encoded at least one field.
                if (newDisplayContents.length > 0) {
                    newContents.append(';')
                    contents = newContents.toString()
                    displayContents = newDisplayContents.toString()
                    title = "Contact"
                } else {
                    contents = null
                    displayContents = null
                }
            }
            QRGContents.Type.LOCATION -> if (bundle != null) {
                // These must use Bundle.getFloat(), not getDouble(), it's part of the API.
                val latitude = bundle.getFloat("LAT", Float.MAX_VALUE)
                val longitude = bundle.getFloat("LONG", Float.MAX_VALUE)
                if (latitude != Float.MAX_VALUE && longitude != Float.MAX_VALUE) {
                    contents = "geo:$latitude,$longitude"
                    displayContents = "$latitude,$longitude"
                    title = "Location"
                }
            }
        }
    }

    fun getBitmap(): Bitmap? {
        return if (!encoded) null else try {
            var hints: MutableMap<EncodeHintType?, Any?>? = null
            val encoding = guessAppropriateEncoding(contents)
            if (encoding != null) {
                hints = EnumMap(EncodeHintType::class.java)
                hints[EncodeHintType.CHARACTER_SET] = encoding
            }
            val writer = MultiFormatWriter()
            val result = writer.encode(contents, format, dimension, dimension, hints)
            val width = result.width
            val height = result.height
            val pixels = IntArray(width * height)
            // All are 0, or black, by default
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (result[x, y]) getColorBlack() else getColorWhite()
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (ex: Exception) {
            null
        }
    }

    private fun guessAppropriateEncoding(contents: CharSequence?): String? {
        // Very crude at the moment
        for (i in 0 until contents!!.length) {
            if (contents[i].toInt() > 0xFF) {
                return "UTF-8"
            }
        }
        return null
    }

    private fun trim(s: String?): String? {
        if (s == null) {
            return null
        }
        val result = s.trim { it <= ' ' }
        return if (result.length == 0) null else result
    }

    private fun escapeVCard(input: String?): String? {
        if (input == null || input.indexOf(':') < 0 && input.indexOf(';') < 0) {
            return input
        }
        val length = input.length
        val result = StringBuilder(length)
        for (i in 0 until length) {
            val c = input[i]
            if (c == ':' || c == ';') {
                result.append('\\')
            }
            result.append(c)
        }
        return result.toString()
    }

    object ImageType {
        var IMAGE_PNG = 0
        var IMAGE_JPEG = 1
        var IMAGE_WEBP = 2
    }

    object Type {
        // Plain text. Use Intent.putExtra(DATA, string). This can be used for URLs too, but string
        // must include "http://" or "https://".
        const val TEXT = "TEXT_TYPE"

        // An email type. Use Intent.putExtra(DATA, string) where string is the email address.
        const val EMAIL = "EMAIL_TYPE"

        // Use Intent.putExtra(DATA, string) where string is the phone number to call.
        const val PHONE = "PHONE_TYPE"

        // An SMS type. Use Intent.putExtra(DATA, string) where string is the number to SMS.
        const val SMS = "SMS_TYPE"
        const val CONTACT = "CONTACT_TYPE"
        const val LOCATION = "LOCATION_TYPE"
    }

    val URL_KEY = "URL_KEY"

    val NOTE_KEY = "NOTE_KEY"

    // When using Type.CONTACT, these arrays provide the keys for adding or retrieving multiple
    // phone numbers and addresses.
    val PHONE_KEYS = arrayOf(
        ContactsContract.Intents.Insert.PHONE, ContactsContract.Intents.Insert.SECONDARY_PHONE,
        ContactsContract.Intents.Insert.TERTIARY_PHONE
    )

    val PHONE_TYPE_KEYS = arrayOf(
        ContactsContract.Intents.Insert.PHONE_TYPE,
        ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
        ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE
    )

    val EMAIL_KEYS = arrayOf(
        ContactsContract.Intents.Insert.EMAIL, ContactsContract.Intents.Insert.SECONDARY_EMAIL,
        ContactsContract.Intents.Insert.TERTIARY_EMAIL
    )

    val EMAIL_TYPE_KEYS = arrayOf(
        ContactsContract.Intents.Insert.EMAIL_TYPE,
        ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE,
        ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE
    )
}