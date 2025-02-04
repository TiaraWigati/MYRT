package com.example.myrt.User

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.myrt.R
import com.example.myrt.UrlClass
import com.example.myrt.databinding.ActivityKegiatanBinding
import org.json.JSONArray

class KegiatanActivity : AppCompatActivity() {
    private lateinit var b: ActivityKegiatanBinding

    lateinit var urlClass: UrlClass

    val dataKegiatan = mutableListOf<HashMap<String,String>>()
    lateinit var kegiatanAdp : AdapterKegiatan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityKegiatanBinding.inflate(layoutInflater)
        setContentView(b.root)
        supportActionBar?.setTitle("Kegiatan RT")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        urlClass = UrlClass()
        kegiatanAdp = AdapterKegiatan(dataKegiatan, this)
        b.rvKegiatan.layoutManager = LinearLayoutManager(this)
        b.rvKegiatan.adapter = kegiatanAdp
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                showDataKegiatan("show_data_kegiatan", query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                showDataKegiatan("show_data_kegiatan", newText)
                return true
            }
        })
        return true
    }

    override fun onStart() {
        super.onStart()
        showDataKegiatan("show_data_kegiatan", "")
    }

    private fun showDataKegiatan(mode: String, nama_kegiatan: String) {
        val request = object : StringRequest(
            Method.POST,urlClass.kegiatan,
            Response.Listener { response ->
                dataKegiatan.clear()
                if (response.equals(0)) {
                    Toast.makeText(this,"Data tidak ditemukan", Toast.LENGTH_LONG).show()
                } else {
                    val jsonArray = JSONArray(response)
                    for (x in 0..(jsonArray.length()-1)){
                        val jsonObject = jsonArray.getJSONObject(x)
                        var  frm = HashMap<String,String>()
                        frm.put("kd_kegiatan",jsonObject.getString("kd_kegiatan"))
                        frm.put("nama_kegiatan",jsonObject.getString("nama_kegiatan"))
                        frm.put("tgl_kegiatan",jsonObject.getString("tgl_kegiatan"))
                        frm.put("jam_kegiatan",jsonObject.getString("jam_kegiatan"))
                        frm.put("nama",jsonObject.getString("nama"))

                        dataKegiatan.add(frm)
                    }
                    kegiatanAdp.notifyDataSetChanged()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this,"Tidak dapat terhubung ke server", Toast.LENGTH_LONG).show()
            }){
            override fun getParams(): MutableMap<String, String>? {
                val hm = HashMap<String,String>()
                when(mode){
                    "show_data_kegiatan" -> {
                        hm.put("mode","show_data_kegiatan")
                        hm.put("nama_kegiatan", nama_kegiatan)
                    }
                }

                return hm
            }
        }
        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    class AdapterKegiatan(val dataKegiatan: List<HashMap<String,String>>, val parent: KegiatanActivity):
        RecyclerView.Adapter<AdapterKegiatan.HolderDataKegiatan>(){
        class HolderDataKegiatan (v : View) : RecyclerView.ViewHolder(v) {
            val nm = v.findViewById<TextView>(R.id.kegiatanNama)
            val tgl = v.findViewById<TextView>(R.id.kegiatanTanggal)
            val dtt = v.findViewById<TextView>(R.id.btnDetail)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderDataKegiatan {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.row_kegiatan, parent, false)
            return HolderDataKegiatan(v)
        }

        override fun getItemCount(): Int {
            return dataKegiatan.size
        }

        override fun onBindViewHolder(holder: HolderDataKegiatan, position: Int) {
            val data = dataKegiatan.get(position)
            holder.nm.setText(data.get("nama_kegiatan"))
            holder.tgl.setText(data.get("tgl_kegiatan"))
            holder.dtt.setOnClickListener {
                val intent = Intent(it.context, KegiatanDetailActivity::class.java)
                intent.putExtra("kode", data.get("kd_kegiatan").toString())
                it.context.startActivity(intent)
                parent.overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
            }
        }
    }
}