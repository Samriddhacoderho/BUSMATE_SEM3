package com.example.busmate.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.example.busmate.model.ChildModel
import com.example.busmate.model.UserModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject // Import JSON utility

object QRCodeGenerator {
    // Generate a JSON string
    fun generateFullDataString(child: ChildModel, parent: UserModel?): String {
        val json = JSONObject()
        json.put("type", "STUDENT_ID") // Identifies this as a BusMate ID
        json.put("studentId", child.studentId)
        json.put("fullName", "${child.firstName} ${child.lastName}")
        json.put("busRouteId", child.busRouteId)
        json.put("parentId", parent?.uid ?: "N/A")
        json.put("contact", parent?.phone ?: "N/A")

        return json.toString() // Returns {"studentId":"123", "fullName":"John Doe", ...}
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