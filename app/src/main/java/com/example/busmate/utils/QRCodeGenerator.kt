package com.example.busmate.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.example.busmate.model.ChildModel
import com.example.busmate.model.UserModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QRCodeGenerator {
    // Encodes all required data into one string for the QR code
    fun generateFullDataString(child: ChildModel, parent: UserModel?): String {
        return """
            Name: ${child.firstName} ${child.lastName}
            Student ID: ${child.studentId}
            Route: ${child.busRouteId}
            Pickup: ${child.pickUpLocation}
            Drop-off: ${child.dropOffLocation}
            Parent: ${parent?.firstName ?: "N/A"} ${parent?.lastName ?: ""}
            Contact: ${parent?.phone ?: "N/A"}
        """.trimIndent()
    }

    fun createQRCode(content: String, size: Int = 512): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}