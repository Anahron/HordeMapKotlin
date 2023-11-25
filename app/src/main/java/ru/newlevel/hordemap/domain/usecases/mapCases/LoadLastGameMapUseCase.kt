package ru.newlevel.hordemap.domain.usecases.mapCases

import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.BASE_LAST_MAP_FILENAME
import ru.newlevel.hordemap.app.GPX_EXTENSION
import ru.newlevel.hordemap.app.KMZ_EXTENSION
import java.io.File

class LoadLastGameMapUseCase() {
    fun execute(context: Context): Result<Uri> {
        var filename = BASE_LAST_MAP_FILENAME + KMZ_EXTENSION
        var file = File(context.filesDir, filename)
        if (!file.exists()) {
            filename = BASE_LAST_MAP_FILENAME + GPX_EXTENSION
            file = File(context.filesDir, filename)
        }
        return if (file.exists())
            Result.success(Uri.fromFile(file))
        else
            Result.failure(Throwable(context.resources.getString(R.string.map_empty)))
    }
}