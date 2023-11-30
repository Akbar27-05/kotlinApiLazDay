package com.example.projectlazdayshowapi

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.projectlazdayshowapi.databinding.ActivityDetailBinding
import com.example.projectlazdayshowapi.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this

        setContentView(binding.root)

        val obj = intent.getStringExtra("id" )
        val json = JSONObject(obj)

        setSupportActionBar(binding.appbar.tbDefault)
        supportActionBar?.title = json.getString("title")

        binding.lifecycleOwner?.lifecycleScope?.launch {
            withContext(Dispatchers.IO){
                URL("${json.getString("image")}").openStream().use {
                    BitmapFactory.decodeStream(it).let { bitmap ->
                        binding.imgD = BitmapDrawable(binding.root.resources, bitmap)
                    }
                }
            }
        }
    }
}