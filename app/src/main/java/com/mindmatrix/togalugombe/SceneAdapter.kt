package com.mindmatrix.togalugombe

import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SceneAdapter(private val scenes: List<Scene>, private val tts: TextToSpeech) : RecyclerView.Adapter<SceneAdapter.SceneViewHolder>() {

    class SceneViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tvSceneNumber)
        val tvKannada: TextView = view.findViewById(R.id.tvSceneKannada)
        val tvEnglish: TextView = view.findViewById(R.id.tvSceneEnglish)
        val btnReadAloud: Button = view.findViewById(R.id.btnReadAloud)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SceneViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scene, parent, false)
        return SceneViewHolder(view)
    }

    override fun onBindViewHolder(holder: SceneViewHolder, position: Int) {
        val scene = scenes[position]
        holder.tvNumber.text = scene.sceneNumber
        holder.tvKannada.text = scene.textKannada
        holder.tvEnglish.text = scene.textEnglish

        // Magic feature: Read the story out loud!
        holder.btnReadAloud.setOnClickListener {
            tts.speak(scene.textEnglish, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun getItemCount() = scenes.size
}