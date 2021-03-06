package de.ph1b.audiobook.misc

import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Analyzes media files for meta data and duration.
 */
class MediaAnalyzer @Inject constructor(
  private val exoPlayerDurationParser: DurationAnalyzer,
  private val metaDataAnalyzer: MetaDataAnalyzer
) {

  suspend fun analyze(file: File): Result {
    val duration = exoPlayerDurationParser.duration(file) ?: -1
    return if (duration > 0) {
      val metaData = metaDataAnalyzer.parse(file)
      Result.Success(
        duration = duration,
        chapterName = metaData.chapterName,
        author = metaData.author,
        bookName = metaData.bookName
      )
    } else {
      Timber.d("ExoPlayer failed to parse $file too.")
      Result.Failure
    }
  }

  sealed class Result {
    data class Success(
      val duration: Int,
      val chapterName: String,
      val author: String?,
      val bookName: String?
    ) : Result() {
      init {
        require(duration > 0)
      }
    }

    object Failure : Result()
  }
}
