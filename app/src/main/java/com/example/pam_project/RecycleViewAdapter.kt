package com.example.pam_project
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pam_project.databinding.RowBinding

class RecycleViewAdapter(
    private val reports: List<Report>,
    private val onEditClick: (Report) -> Unit,
    private val onDeleteClick: (Report) -> Unit
) : RecyclerView.Adapter<RecycleViewAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(private val binding: RowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            binding.apply {
                textTitle.text = report.title
                textDescription.text = report.description
                Glide.with(itemView.context)
                    .load(report.imageUrl)
                    .into(imageView)

                btnEdit.setOnClickListener {
                    onEditClick(report)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(report)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = RowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.bind(report)
    }

    override fun getItemCount(): Int {
        return reports.size
    }
}
