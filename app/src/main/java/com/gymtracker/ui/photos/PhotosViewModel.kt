package com.gymtracker.ui.photos

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.db.entity.BodyPhoto
import com.gymtracker.data.db.entity.BodyZone
import com.gymtracker.data.repository.BodyPhotoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosViewModel(private val repo: BodyPhotoRepository) : ViewModel() {

    private val _selectedZone = MutableStateFlow<BodyZone?>(null)

    val photos: StateFlow<List<BodyPhoto>> = _selectedZone.flatMapLatest { zone ->
        if (zone == null) repo.getAllPhotos() else repo.getPhotosByZone(zone)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedZone: StateFlow<BodyZone?> = _selectedZone.asStateFlow()

    fun selectZone(zone: BodyZone?) { _selectedZone.value = zone }

    fun savePhoto(context: Context, uri: Uri, zone: BodyZone) {
        viewModelScope.launch {
            val destFile = File(context.filesDir, "body_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            repo.insertPhoto(BodyPhoto(date = System.currentTimeMillis(), zone = zone, photoPath = destFile.absolutePath))
        }
    }

    class Factory(private val repo: BodyPhotoRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PhotosViewModel(repo) as T
        }
    }
}
