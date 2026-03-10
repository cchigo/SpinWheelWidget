package com.chigo.tappgospinwheel.data.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.chigo.tappgospinwheel.util.md5
import java.io.File
import java.io.FileOutputStream

 /**
  * Two-level image cache: memory (fast, session-only) and disk (persistent across app launches).
 Images are stored on disk using the MD5 hash of their remote URL as the filename.
 On next access, disk images are promoted to memory for faster retrieval.

**/
class WheelImageCache(context: Context) {

    private val cacheDir = context.applicationContext.cacheDir
    private val memoryCache = mutableMapOf<String, Bitmap>()

/**
Write
    Takes the raw bytes downloaded from the network and saves them to disk.
    The filename is the MD5 hash of the remote URL, so the same URL always maps to the same file.
**/
    fun saveImage(url: String, bytes: ByteArray) {
        val file = File(cacheDir, url.md5())
        FileOutputStream(file).use { it.write(bytes) }
    }

     /*Read
     * Looks up a bitmap by its remote URL.
      Checks memory first, then disk. Returns null if neither has it, meaning a network fetch is needed.
     * */

    fun loadImage(url: String): Bitmap? {
        memoryCache[url]?.let { return it }

        val file = File(cacheDir, url.md5())
        if (!file.exists()) return null

        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
        memoryCache[url] = bitmap
        return bitmap
    }

    /*Cleanup
    * Called after a fresh config is fetched. Deletes any disk files and memory entries
    * whose URLs are no longer referenced by the current config, keeping the cache lean.
     */

    fun cleanup(activeUrls: Set<String>) {
        val activeFiles = activeUrls.map { it.md5() }.toSet()
        cacheDir.listFiles()?.forEach { file ->
            if (file.name !in activeFiles) file.delete()
        }
        memoryCache.keys.retainAll(activeUrls)
    }
}