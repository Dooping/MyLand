package com.gago.david.myland.utils

import android.net.Uri
import java.io.File
import java.io.IOException

object Utils {
    /**
     * Convert a uri generated by a fileprovider, like content://AUTHORITY/ROOT/actual/path
     * to a file pointing to file:///actual/path
     *
     * Note that it only works for paths generated with `ROOT` as the path element. This is done if
     * nnf_provider_paths.xml is used to define the file provider in the manifest.
     *
     * @param uri generated from a file provider
     * @return Corresponding [File] object
     */
    fun getFileForUri(uri: Uri): File {
        var path = uri.encodedPath
        val splitIndex = path!!.indexOf('/', 1)
        val tag = Uri.decode(path.substring(1, splitIndex))
        path = Uri.decode(path.substring(splitIndex + 1))
        require("root".equals(tag, ignoreCase = true)) {
            String.format("Can't decode paths to '%s', only for 'root' paths.",
                    tag)
        }
        val root = File("/")
        var file = File(root, path)
        file = try {
            file.canonicalFile
        } catch (e: IOException) {
            throw IllegalArgumentException("Failed to resolve canonical path for $file")
        }
        if (!file.path.startsWith(root.path)) {
            throw SecurityException("Resolved path jumped beyond configured root")
        }
        return file
    }
}