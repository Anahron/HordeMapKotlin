package ru.newlevel.hordemap.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract


class SelectFilesContract : ActivityResultContract<String, Uri?>() {

    override fun createIntent(context: Context, input: String): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = input
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? = when {


        resultCode != Activity.RESULT_OK -> null
        else -> intent?.data
    }
}