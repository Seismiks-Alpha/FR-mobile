package com.seismiks.nyamapp.ui.history

import HistoryViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.remote.response.HistoryAllResponse
import com.seismiks.nyamapp.data.remote.response.HistoryTodayResponse
import com.seismiks.nyamapp.data.remote.response.HistoryViewItem
import com.seismiks.nyamapp.databinding.ActivityHistoryBinding
import com.seismiks.nyamapp.ui.ContainerActivity
import com.seismiks.nyamapp.ui.detail.HistoryDetailActivity
import com.seismiks.nyamapp.utils.FOOD_TODAY
import com.seismiks.nyamapp.utils.HistoryUtil
import com.seismiks.nyamapp.utils.HistoryUtil.toHistoryViewItem

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var viewModel: HistoryViewModel
    private lateinit var auth: FirebaseAuth
    private var time: String = "today"
    private var fullHistoryResponse: HistoryTodayResponse? = null
    private var fullHistoryAll: HistoryAllResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.title = getString(R.string.scan_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        rvHistory = binding.rvResultList

        binding.btnSort.setOnClickListener {
            showPickerMenu(binding.myAppbar, R.menu.history_time)
        }

        binding.btnDeleteAll.setOnClickListener {
            auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result?.token
                    if (idToken != null) {
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Konfirmasi")
                            .setMessage("Apakah Anda yakin ingin menghapus semua riwayat? Aksi ini tidak dapat dibatalkan.")
                            .setPositiveButton("Hapus") { _, _ ->
                                viewModel.deleteHistoryAll(idToken).observe(this) { result ->
                                    if (result is Result.Success) {
                                        val intent = Intent(this, ContainerActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                        finish()
                                    } else if (result is Result.Error) {
                                        Log.e("HistoryActivity", result.error)
                                    }
                                }
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    } else {
                        Log.e("HistoryActivity", "ID Token is null")
                    }
                }
            }
        }

        setupRecyclerView()

        fetchHistoryData()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter { historyViewItem ->
            val detailIntent = Intent(this, HistoryDetailActivity::class.java).apply {
                putExtra(FOOD_TODAY, historyViewItem)
            }
            startActivity(detailIntent)
        }
        binding.rvResultList.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }
    }

    private fun fetchHistoryData() {
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result?.token
                if (idToken != null) {
                    // Cek 'time' untuk menentukan endpoint mana yang harus dipanggil
                    if (time == "today" || time == "yesterday") {
                        viewModel.getHistoryToday(idToken).observe(this) { result ->
                            handleResultToday(result)
                        }
                    } else { // time == "all"
                        viewModel.getHistoryAll(idToken).observe(this) { result ->
                            handleResultAll(result)
                        }
                    }
                } else {
                    Log.e("HistoryActivity", "ID Token is null")
                }
            } else {
                Log.e("HistoryActivity", "Failed to get ID Token", task.exception)
            }
        }
    }

    private fun handleResultToday(result: Result<HistoryTodayResponse>) {
        when (result) {
            is Result.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
            }

            is Result.Success -> {
                binding.progressBar.visibility = View.GONE
                fullHistoryResponse = result.data
                updateListBasedOnFilter()
            }

            is Result.Error -> {
                binding.progressBar.visibility = View.GONE
                fullHistoryResponse = null
                updateListBasedOnFilter()
                Toast.makeText(this, result.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleResultAll(result: Result<HistoryAllResponse>) {
        when (result) {
            is Result.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
            }

            is Result.Success -> {
                binding.progressBar.visibility = View.GONE
                fullHistoryAll = result.data
                updateListBasedOnFilter()
            }

            is Result.Error -> {
                binding.progressBar.visibility = View.GONE
                fullHistoryResponse = null
                updateListBasedOnFilter()
                Toast.makeText(this, result.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateListBasedOnFilter() {
        val listToSubmit: List<HistoryViewItem> = when (time) {
            "today" -> {
                fullHistoryResponse?.today?.foodHistory
                    ?.map { it.toHistoryViewItem() }
                    ?: emptyList()
            }

            "yesterday" -> {
                fullHistoryResponse?.yesterday?.foodHistory
                    ?.map { it.toHistoryViewItem() }
                    ?: emptyList()
            }

            "all" -> {
                fullHistoryAll?.records
                    ?.filterNotNull()
                    ?.map { it.toHistoryViewItem() }
                    ?: emptyList()
            }

            else -> emptyList()
        }

        // Sekarang adapter Anda menerima tipe data yang konsisten
        adapter.submitList(listToSubmit)

        if (listToSubmit.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.btnDeleteAll.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            if (time == "all") {
                if (fullHistoryAll == null) {
                    binding.btnDeleteAll.visibility = View.VISIBLE
                    binding.btnDeleteAll.isEnabled = false
                } else {
                    binding.btnDeleteAll.visibility = View.VISIBLE
                    binding.btnDeleteAll.isEnabled = true
                }
            } else {
                binding.btnDeleteAll.visibility = View.GONE
            }
        }
    }

    private fun showPickerMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            val newTime = when (menuItem.itemId) {
                R.id.today -> "today"
                R.id.yesterday -> "yesterday"
                R.id.all -> "all"
                else -> return@setOnMenuItemClickListener false
            }

            if (time != newTime) {
                time = newTime
                binding.btnSort.text = menuItem.title

                // Cek apakah data untuk filter baru sudah ada. Jika tidak, fetch lagi.
                val needsFetching = when (time) {
                    "all" -> fullHistoryAll == null
                    else -> fullHistoryResponse == null
                }

                if (needsFetching) {
                    fetchHistoryData()
                } else {
                    // Jika data sudah ada, cukup update UI
                    updateListBasedOnFilter()
                }
            }
            true
        }
        popup.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, ContainerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
        return true
    }
}