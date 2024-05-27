package com.example.pam_project

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pam_project.databinding.ActivityReportBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var binding : ActivityReportBinding
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private var imageUri: Uri? = null
    private lateinit var auth: FirebaseAuth

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                imageUri = data.data
                if (imageUri != null) {
                    binding.uploadImage.isEnabled = true
                    Glide.with(this).load(imageUri).into(binding.imageView)
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
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("reports")

        binding.uploadImage.isEnabled = false

        binding.listButton.setOnClickListener {
            val intent = Intent(this@ReportActivity, ListActivity::class.java)
            startActivity(intent)
        }

        binding.selectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            activityResultLauncher.launch(intent)
        }

        binding.uploadImage.setOnClickListener {
            imageUri?.let { uri ->
                val title = binding.titleEditText.text.toString().trim()
                val description = binding.descriptionEditText.text.toString().trim()

                if (title.isNotEmpty() && description.isNotEmpty()) {
                    uploadImage(uri, title, description)
                } else {
                    Toast.makeText(this, "Silakan isi judul dan deskripsi", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Silakan pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
        }

    }

    private fun uploadImage(file: Uri, title: String, description: String) {
        val userId = auth.currentUser?.uid
        val userReportsRef = userId?.let {
            databaseReference.child(it)
        }

        val ref = storageReference.child("images/${UUID.randomUUID()}")
        ref.putFile(file)
            .addOnSuccessListener { taskSnapshot ->
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveReportToDatabase(userReportsRef, title, description, uri.toString())
                    // Setelah berhasil mengunggah, kosongkan input dan reset ImageView
                    binding.titleEditText.setText("")
                    binding.descriptionEditText.setText("")
                    binding.imageView.setImageDrawable(null)
                    binding.uploadImage.isEnabled = false
                }
                Toast.makeText(this, "Gambar Berhasil Diupload!!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal! ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { taskSnapshot ->
                binding.progress.max = taskSnapshot.totalByteCount.toInt()
                binding.progress.progress = taskSnapshot.bytesTransferred.toInt()
            }
    }

    private fun saveReportToDatabase(
        userReportsRef: DatabaseReference?,
        title: String,
        description: String,
        imageUrl: String
    ) {
        val reportId = userReportsRef?.push()?.key
        val reportData = HashMap<String, Any>()
        reportData["title"] = title
        reportData["description"] = description
        reportData["imageUrl"] = imageUrl

        reportId?.let { key ->
            userReportsRef.child(key).setValue(reportData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data Laporan Tersimpan!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan data laporan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun logOut() {
        auth.signOut()
        val intent = Intent(this@ReportActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
