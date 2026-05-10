package com.mindmatrix.togalugombe

import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PuppetAdapter(private var puppets: List<Puppet>) : RecyclerView.Adapter<PuppetAdapter.PuppetViewHolder>() {

    class PuppetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivPuppetImage)
        val tvNameKn: TextView = view.findViewById(R.id.tvPuppetNameKn)
        val tvNameEn: TextView = view.findViewById(R.id.tvPuppetNameEn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PuppetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_puppet, parent, false)
        return PuppetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PuppetViewHolder, position: Int) {
        val puppet = puppets[position]
        holder.tvNameKn.text = puppet.name_kn
        holder.tvNameEn.text = puppet.name_en
        
        val context = holder.ivImage.context
        val imageResId = context.resources.getIdentifier(puppet.local_image, "drawable", context.packageName)
        if (imageResId != 0) holder.ivImage.setImageResource(imageResId)

        holder.itemView.setOnClickListener {
            // Show Custom Premium Dialog
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_puppet_detail)
            
            // Make background transparent for rounded corners
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val dImage = dialog.findViewById<ImageView>(R.id.dialogImage)
            val dTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
            val dPowers = dialog.findViewById<TextView>(R.id.dialogPowers)
            val dDesc = dialog.findViewById<TextView>(R.id.dialogDesc)
            val btnShare = dialog.findViewById<Button>(R.id.btnShare)
            val btnClose = dialog.findViewById<Button>(R.id.btnClose)

            if (imageResId != 0) dImage.setImageResource(imageResId)
            dTitle.text = "${puppet.name_kn} (${puppet.name_en})"
            dPowers.text = "Powers: ${puppet.powers}"
            dDesc.text = puppet.description_en

            // Feature Upgrade: Share puppet info to other apps
            btnShare.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this Togalu Gombe character!\n\nName: ${puppet.name_en}\nPowers: ${puppet.powers}\n\n${puppet.description_en}")
                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
            }

            btnClose.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }

    override fun getItemCount() = puppets.size

    fun updateList(filteredList: List<Puppet>) {
        puppets = filteredList
        notifyDataSetChanged()
    }
}