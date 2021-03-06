package de.ph1b.audiobook.misc

import android.media.MediaMetadataRetriever
import java.io.File
import javax.inject.Inject

/**
 * Extracts meta data from media files. This class is thread safe.
 */
class MetaDataAnalyzer @Inject constructor() {

  private val mmr = MediaMetadataRetriever()

  @Synchronized
  fun parse(file: File): MetaData {
    val chapterNameFallback = chapterNameFallback(file)
    // try preparing twice as MediaMetadataRetriever throws undocumented exceptions
    repeat(2) {
      if (prepare(file)) {
        val chapterName = parseChapterName()
          ?: chapterNameFallback
        val bookName = parseBookName()
        val author = parseAuthor()
        return MetaData(chapterName = chapterName, bookName = bookName, author = author)
      }
    }
    return MetaData(chapterName = chapterNameFallback, bookName = null, author = null)
  }

  private fun prepare(file: File): Boolean {
    // Note: MediaMetadataRetriever throws undocumented Exceptions. We catch these
    // and act appropriate.
    return try {
      mmr.setDataSource(file.absolutePath)
      true
    } catch (e: RuntimeException) {
      false
    }
  }

  private fun parseChapterName(): String? {
    return mmr.safeExtract(MediaMetadataRetriever.METADATA_KEY_TITLE)
      ?.takeUnless { it.isEmpty() }
  }

  private fun chapterNameFallback(file: File): String {
    return file.nameWithoutExtension
      .trim()
      .takeUnless { it.isEmpty() }
      ?: file.name
  }

  private fun parseBookName(): String? = mmr.safeExtract(MediaMetadataRetriever.METADATA_KEY_ALBUM)

  private fun parseAuthor(): String? {
    return mmr.safeExtract(MediaMetadataRetriever.METADATA_KEY_ARTIST)
      ?.takeUnless { it.isEmpty() }
      ?: mmr.safeExtract(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
  }

  /**
   * As [MediaMetadataRetriever] has several bugs it is important to catch the exception here as
   * it randomly throws [RuntimeException] on certain implementations.
   */
  private fun MediaMetadataRetriever.safeExtract(key: Int): String? {
    return try {
      extractMetadata(key)
    } catch (ignored: Exception) {
      null
    }
  }

  data class MetaData(
    val chapterName: String,
    val bookName: String?,
    val author: String?
  )
}
