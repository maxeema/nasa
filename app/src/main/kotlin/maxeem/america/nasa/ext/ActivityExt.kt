package maxeem.america.nasa.ext

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import maxeem.america.common.Str
import maxeem.america.nasa.R
import maxeem.america.nasa.app
import maxeem.america.nasa.misc.AppException
import maxeem.america.nasa.ui.ComposeActivity
import java.io.File
import java.util.*

/**
 * Activity extensions
 */

inline fun <reified T: Activity>
        ComposeActivity.start(extras: Bundle? = null) =
    startActivity(Intent(this, T::class.java).apply {
        extras?.also {
            putExtras(extras)
        }
    })

fun ComposeActivity.inLandscape() =
    resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun ComposeActivity.showMsg(title: Str? = null, msg: Str? = null) {
    AlertDialog.Builder(this)
        .setTitle(title).setMessage(msg)
        .setPositiveButton(R.string.ok.asString()) { _, _ -> }
        .show()
}
fun ComposeActivity.handleError(error: AppException) {
    error.err?.printStackTrace()
    error.desc?.also { app.lg { it } }
    Toast.makeText(this, error.msg, Toast.LENGTH_SHORT).show()
}

fun ComposeActivity.selectDate(calendar: Calendar?,
                               onShow: ((DialogInterface)->Unit)? = null,
                               onDismiss: ((DialogInterface)->Unit)? = null,
                               onSelected: (result: Calendar)->Unit) {
    val c = calendar ?: Calendar.getInstance()
    DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        onSelected(Calendar.getInstance().apply {
            clear()
            set(year, month, dayOfMonth)
        })
    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).apply {
        setOnShowListener(onShow)
        setOnDismissListener(onDismiss)
    }.show()
}

fun ComposeActivity.onWriteAvailable(path: File?, onSuccess: (toDir: File)->Unit) {
    runCatching {
        path ?: throw AppException(R.string.storage_unavailable.asString())
        when (Environment.getExternalStorageState(path)) {
            Environment.MEDIA_MOUNTED -> {
                path.takeIf { it.isDirectory || it.mkdirs() }?.also {
                    onSuccess(it)
                } ?: throw AppException(R.string.storage_access_error.asString())
            }
            Environment.MEDIA_MOUNTED_READ_ONLY -> throw AppException(
                R.string.storage_read_only.asString()
            )
            else -> throw AppException(R.string.storage_unavailable.asString())
        }
    }.onFailure {
        handleError(it.ensureApp())
    }

}
