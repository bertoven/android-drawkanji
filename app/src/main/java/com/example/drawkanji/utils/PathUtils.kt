package com.example.drawkanji.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

fun getPath(context: Context, uri: Uri): String? {
    var newUri = uri
    var selection: String? = null
    var selectionArgs: Array<String>? = null

    if (DocumentsContract.isDocumentUri(context.applicationContext, newUri)) {
        when {
            isExternalStorageDocument(newUri) -> {
                val docId = DocumentsContract.getDocumentId(newUri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return "${Environment.getExternalStorageDirectory()}/${split[1]}"
            }
            isDownloadsDocument(newUri) -> {
                val id = DocumentsContract.getDocumentId(newUri)
                newUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
            }
            isMediaDocument(newUri) -> {
                val docId = DocumentsContract.getDocumentId(newUri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                when (split[0]) {
                    "image" -> newUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> newUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> newUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
    }
    if ("content".equals(newUri.scheme, ignoreCase = true)) {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor?
        try {
            cursor =
                context.contentResolver.query(newUri, projection, selection, selectionArgs, null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex)
            }
            cursor.close()
        } catch (e: Exception) {
        }

    } else if ("file".equals(newUri.scheme, ignoreCase = true)) {
        return newUri.path
    }
    return null
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}