package com.example.projectlazdayshowapi

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.DrawFilter
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectlazdayshowapi.databinding.ActivityMainBinding
import com.example.projectlazdayshowapi.databinding.WidgetItemBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this

        setContentView(binding.root)

        showData()

    }

    private fun showData(){
        val han = CoroutineExceptionHandler { _, e ->
            if (e is Exception){
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Error")
                    .setMessage(e.message)
                    .setNeutralButton("Ok"){_,_->}
                    .create()
                    .show()
            }else{
                throw RuntimeException(e)
            }
        }

        lifecycleScope.launch(han) {
            withContext(Dispatchers.IO){
                val conn = URL("https://demo.lazday.com/rest-api-sample/data.php").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val body = conn.inputStream?.bufferedReader()?.use { it.readLine() }

                withContext(Dispatchers.Main){
                    if (body == null){
                        throw Exception("Data is null")
                    }

                    val json = JSONObject(body!!).optJSONArray("result")

                    binding.rvApiLaz.layoutManager = LinearLayoutManager(this@MainActivity)
                    binding.rvApiLaz.adapter = LazzAdapter(json){
                        Intent(this@MainActivity, DetailActivity::class.java).apply {
                            putExtra("id", it)
                        }.let {
                            startActivity(it)
                        }
                    }
                }
            }
        }
    }

    class LazzAdapter(private val items: JSONArray, private val callback: (id: String) -> Unit) : RecyclerView.Adapter<LazzAdapter.LazzViewHolder>(){
        class LazzViewHolder(val binding: WidgetItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LazzViewHolder {
            val binding = WidgetItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.lifecycleOwner = parent.findViewTreeLifecycleOwner()
            return LazzViewHolder(binding)
        }

        override fun getItemCount(): Int = items.length()

        override fun onBindViewHolder(holder: LazzViewHolder, position: Int) {
            val json = items.getJSONObject(position)

            val datas = json.toString();

            val img = json.getString("image")

            holder.binding.root.setOnClickListener {
                callback(datas)
            }

            holder.binding.titel = json.getString("title")
            holder.binding.lifecycleOwner?.lifecycleScope?.launch {
                withContext(Dispatchers.IO){
                    val url = URL(img)
                    val inputStream = url.openStream()
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)

                    val targetWidth = holder.binding.ivImg.width
                    val targetHeight = holder.binding.ivImg.height

                    val scaleX = targetWidth.toFloat() / originalBitmap.width
                    val scaleY = targetHeight.toFloat() / originalBitmap.height

                    val matrix = Matrix()
                    matrix.setScale(scaleX, scaleY)

                    val scaledBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
                    
                    withContext(Dispatchers.Main) {
                        holder.binding.ivImg.setImageBitmap(scaledBitmap)
                    }
                }
            }
        }
    }
}