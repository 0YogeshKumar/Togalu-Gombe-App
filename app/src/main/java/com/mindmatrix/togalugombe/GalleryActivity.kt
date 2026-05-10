package com.mindmatrix.togalugombe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.widget.Button
import android.content.Intent

class GalleryActivity : AppCompatActivity() {

    private lateinit var puppetList: List<Puppet>
    private lateinit var adapter: PuppetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPuppets)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        val jsonString = applicationContext.assets.open("puppets.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<Puppet>>() {}.type
        puppetList = Gson().fromJson(jsonString, listType)

        adapter = PuppetAdapter(puppetList)
        recyclerView.adapter = adapter

        // NEW: Live Search Logic
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = puppetList.filter { 
                    it.name_en.lowercase().contains(query) || it.name_kn.contains(query) 
                }
                adapter.updateList(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val btnWebSearch = findViewById<Button>(R.id.btnWebSearch)
        btnWebSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                val intent = Intent(this, WebSearchActivity::class.java)
                intent.putExtra("SEARCH_QUERY", query)
                startActivity(intent)
            } else {
                android.widget.Toast.makeText(this, "Type a character name first!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}