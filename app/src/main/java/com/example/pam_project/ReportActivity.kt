package com.example.pam_project

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var imageView: ImageView
    private lateinit var selectImage: MaterialButton
    private lateinit var uploadImage: MaterialButton
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private var imageUri: Uri? = null

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                imageUri = data.data
                if (imageUri != null) {
                    uploadImage.isEnabled = true
                    Glide.with(this).load(imageUri).into(imageView)
                } else {
                    Toast.makeText(this, "Gagal mendapatkan URI gambar", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Gagal mendapatkan data dari intent", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        FirebaseApp.initializeApp(this)
        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("reports")

        progressIndicator = findViewById(R.id.progress)
        imageView = findViewById(R.id.imageView)
        selectImage = findViewById(R.id.selectImage)
        uploadImage = findViewById(R.id.uploadImage)
        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)

        uploadImage.isEnabled = false

        selectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            activityResultLauncher.launch(intent)
        }

        uploadImage.setOnClickListener {
            imageUri?.let { uri ->
                val title = titleEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()

                if (title.isNotEmpty() && description.isNotEmpty()) {
                    uploadImage(uri, title, description)
                } else {
                    Toast.makeText(this, "Silakan isi judul dan deskripsi", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Silakan pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImage(file: Uri, title: String, description: String) {
        val ref = storageReference.child("images/${UUID.randomUUID()}")
        ref.putFile(file)
            .addOnSuccessListener { taskSnapshot ->
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveReportToDatabase(title, description, uri.toString())
                    titleEditText.setText("")
                    descriptionEditText.setText("")
                    imageView.setImageDrawable(null)
                    uploadImage.isEnabled = false
                }
                Toast.makeText(this, "Gambar Berhasil Diupload!!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal! ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { taskSnapshot ->
                progressIndicator.max = taskSnapshot.totalByteCount.toInt()
                progressIndicator.progress = taskSnapshot.bytesTransferred.toInt()
            }
    }

    private fun saveReportToDatabase(title: String, description: String, imageUrl: String) {
        val reportId = databaseReference.push().key
        val reportData = HashMap<String, Any>()
        reportData["title"] = title
        reportData["description"] = description
        reportData["imageUrl"] = imageUrl

        reportId?.let { key ->
            databaseReference.child(key).setValue(reportData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data Laporan Tersimpan!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan data laporan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
