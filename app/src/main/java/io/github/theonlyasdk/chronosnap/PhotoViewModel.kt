package io.github.theonlyasdk.chronosnap

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ExifMetadata(
    val dateOriginal: String? = null,
    val dateDigitized: String? = null,
    val dateDateTime: String? = null
)

sealed class PhotoStatus {
    object Idle : PhotoStatus()
    object Loading : PhotoStatus()
    object Success : PhotoStatus()
    object Saving : PhotoStatus()
    object SaveSuccess : PhotoStatus()
    data class Error(val message: String) : PhotoStatus()
}

data class PhotoState(
    val selectedUri: Uri? = null,
    val metadata: ExifMetadata? = null,
    val status: PhotoStatus = PhotoStatus.Idle,
    val pendingDateTime: LocalDateTime? = null,
    val pendingIntentSender: android.content.IntentSender? = null
)

class PhotoViewModel : ViewModel() {

    private val _state = MutableStateFlow(PhotoState())
    val state: StateFlow<PhotoState> = _state.asStateFlow()

    private val exifFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

    fun onImageSelected(uri: Uri?, context: Context) {
        if (uri == null) {
            _state.value = PhotoState()
            return
        }

        _state.value = _state.value.copy(
            selectedUri = uri,
            status = PhotoStatus.Loading,
            pendingDateTime = null
        )

        viewModelScope.launch {
            try {
                val metadata = extractMetadata(uri, context)
                val initialDateTime = metadata.dateOriginal?.let {
                    try {
                        LocalDateTime.parse(it, exifFormatter)
                    } catch (e: Exception) {
                        null
                    }
                } ?: metadata.dateDateTime?.let {
                    try {
                        LocalDateTime.parse(it, exifFormatter)
                    } catch (e: Exception) {
                        null
                    }
                }

                _state.value = _state.value.copy(
                    metadata = metadata,
                    status = PhotoStatus.Success,
                    pendingDateTime = initialDateTime ?: LocalDateTime.now()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    status = PhotoStatus.Error(e.message ?: "Unknown error")
                )
            }
        }
    }

    fun onDateChanged(dateMillis: Long?) {
        if (dateMillis == null) return
        val newDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        
        val currentDateTime = _state.value.pendingDateTime ?: LocalDateTime.now()
        _state.value = _state.value.copy(
            pendingDateTime = currentDateTime.withYear(newDate.year)
                .withMonth(newDate.monthValue)
                .withDayOfMonth(newDate.dayOfMonth)
        )
    }

    fun onTimeChanged(hour: Int, minute: Int) {
        val currentDateTime = _state.value.pendingDateTime ?: LocalDateTime.now()
        _state.value = _state.value.copy(
            pendingDateTime = currentDateTime.withHour(hour).withMinute(minute)
        )
    }

    fun saveChanges(context: Context) {
        val uri = _state.value.selectedUri ?: return
        val newDateTime = _state.value.pendingDateTime ?: return

        _state.value = _state.value.copy(status = PhotoStatus.Saving)

        viewModelScope.launch {
            try {
                writeMetadata(context, uri, newDateTime)
                _state.value = _state.value.copy(
                    status = PhotoStatus.SaveSuccess,
                    metadata = extractMetadata(uri, context) // Refresh metadata
                )
            } catch (e: SecurityException) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && e is android.app.RecoverableSecurityException) {
                    _state.value = _state.value.copy(
                        status = PhotoStatus.Idle, // Reset to allow retry
                        pendingIntentSender = e.userAction.actionIntent.intentSender
                    )
                } else {
                    _state.value = _state.value.copy(
                        status = PhotoStatus.Error("Security error: ${e.message}")
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    status = PhotoStatus.Error("Failed to save: ${e.message}")
                )
            }
        }
    }

    fun onIntentSenderConsumed() {
        _state.value = _state.value.copy(pendingIntentSender = null)
    }

    fun onStatusMessageShown() {
        if (_state.value.status is PhotoStatus.SaveSuccess || _state.value.status is PhotoStatus.Error) {
            _state.value = _state.value.copy(status = PhotoStatus.Success)
        }
    }

    private suspend fun writeMetadata(context: Context, uri: Uri, dateTime: LocalDateTime) = withContext(Dispatchers.IO) {
        val formattedDate = dateTime.format(exifFormatter)
        
        // Use ContentResolver to open the file for writing
        // For Scoped Storage (API 29+), we need to use a file descriptor or open an output stream
        // However, ExifInterface requires a File, FileDescriptor, or InputStream (for reading only usually).
        // For writing, it can take a File or FileDescriptor (since 1.2.0-alpha01).
        
        context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
            val exif = ExifInterface(pfd.fileDescriptor)
            exif.setAttribute(ExifInterface.TAG_DATETIME, formattedDate)
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, formattedDate)
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, formattedDate)
            exif.saveAttributes()
        } ?: throw IOException("Could not open FileDescriptor for Uri: $uri")
    }

    private suspend fun extractMetadata(uri: Uri, context: Context): ExifMetadata = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                ExifMetadata(
                    dateOriginal = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL),
                    dateDigitized = exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED),
                    dateDateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
                )
            } ?: throw IOException("Could not open input stream for Uri: $uri")
        } catch (e: Exception) {
            throw e
        }
    }
}
