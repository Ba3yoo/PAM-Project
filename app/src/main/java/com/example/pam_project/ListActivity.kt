package com.example.pam_project

import com.example.pam_project.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pam_project.databinding.ActivityListBinding
import com.example.pam_project.databinding.EditpopupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private lateinit var bind2: EditpopupBinding
    private lateinit var database: DatabaseReference
    private lateinit var reportAdapter: RecycleViewAdapter
    private val reportList = mutableListOf<Report>()
    private lateinit var currentUserID: String
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        database = FirebaseDatabase.getInstance().getReference("reports")

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUserID = currentUser?.uid ?: ""

        binding.rvRecyclerView.layoutManager = LinearLayoutManager(this)
        reportAdapter = RecycleViewAdapter(reportList, this::onEditClick, this::onDeleteClick)
        binding.rvRecyclerView.adapter = reportAdapter

        fetchReports()

        binding.logout2.setOnClickListener {
            auth!!.signOut()
            val intent = Intent(this@ListActivity,
                MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK //makesure user cant go back
            startActivity(intent)
        }
    }

    private fun fetchReports() {
        val userReportsRef = database.child(currentUserID)
        userReportsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reportList.clear()
                for (dataSnapshot in snapshot.children) {
                    val report = dataSnapshot.getValue(Report::class.java)
                    report?.let {
                        it.id = dataSnapshot.key
                        reportList.add(it)
                    }
                }
                reportAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun onEditClick(report: Report) {
        val Ref = database.child(currentUserID).child(report.id?:"")
        val dialogBuilder: android.app.AlertDialog = android.app.AlertDialog.Builder(this).create()
        bind2 = EditpopupBinding.inflate(layoutInflater)

            dialogBuilder.setView(bind2.root)
            dialogBuilder.show()

        bind2.cancelButton.setOnClickListener {
                dialogBuilder.dismiss()
        }
        bind2.confirmButton.setOnClickListener {
            Ref.child("title").setValue(bind2.titleUpdate.text.toString()).addOnSuccessListener {
                Ref.child("description").setValue(bind2.textUpdate.text.toString()).addOnSuccessListener {
                    Toast.makeText(this, "Report updated successfully", Toast.LENGTH_SHORT).show()
                    reportAdapter.notifyDataSetChanged()
                    dialogBuilder.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to update report", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to update report", Toast.LENGTH_SHORT).show()
            }

        }
//        dialogView.findViewById(R.id.cancelButton).setOnClickListener(object : View.OnClickListener() {
//                dialogBuilder.dismiss()
//        }
//        dialogView.findViewById(R.id.confirmButton).setOnClickListener(object : View.OnClickListener() {
//                dialogBuilder.dismiss()
//        }


    }

    private fun onDeleteClick(report: Report) {
        val imageUrl = report.imageUrl ?: return

        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

        storageReference.delete().addOnSuccessListener{
            val reportRef = database.child(currentUserID).child(report.id ?: "")
            reportRef.removeValue().addOnSuccessListener {
                Toast.makeText(this, "Report deleted successfully", Toast.LENGTH_SHORT).show()
                reportList.remove(report)
                reportAdapter.notifyDataSetChanged()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to delete report", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
