package com.example.pam_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pam_project.databinding.ActivityListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private lateinit var database: DatabaseReference
    private lateinit var reportAdapter: RecycleViewAdapter
    private val reportList = mutableListOf<Report>()
    private lateinit var currentUserID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("reports")

        // Get current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUserID = currentUser?.uid ?: ""

        // Set up RecyclerView
        binding.rvRecyclerView.layoutManager = LinearLayoutManager(this)
        reportAdapter = RecycleViewAdapter(reportList, this::onEditClick, this::onDeleteClick)
        binding.rvRecyclerView.adapter = reportAdapter

        // Fetch reports from Firebase
        fetchReports()
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
        // Implement edit functionality
    }

    private fun onDeleteClick(report: Report) {
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
