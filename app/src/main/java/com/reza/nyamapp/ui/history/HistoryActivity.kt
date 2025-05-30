package com.reza.nyamapp.ui.history

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.reza.nyamapp.R
import com.reza.nyamapp.ViewModelFactory
import com.reza.nyamapp.data.local.ScanResult
import com.reza.nyamapp.databinding.ActivityHistoryBinding
import com.reza.nyamapp.ui.detail.HistoryDetailActivity
import com.reza.nyamapp.utils.Event
import com.reza.nyamapp.utils.RESULT_ID

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var viewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarHistoryActivity)
        supportActionBar?.title = getString(R.string.scan_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        rvHistory = binding.rvResultList
        rvHistory.layoutManager = LinearLayoutManager(this)
        
        val itemTouchHelper = ItemTouchHelper(ItemTouchCallBack())
        itemTouchHelper.attachToRecyclerView(rvHistory)

        adapter = HistoryAdapter {
            val detailIntent = Intent(this, HistoryDetailActivity::class.java)
            detailIntent.putExtra(RESULT_ID, it.id)
            startActivity(detailIntent)
        }

        rvHistory.adapter = adapter

        viewModel.allResults.observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }

    private fun showSnackBar(eventMessage: Event<Int>) {
        val message = eventMessage.getContentIfNotHandled() ?: return
        Snackbar.make(
            findViewById(R.id.vg_historyActivity),
            getString(message),
            Snackbar.LENGTH_SHORT
        ).setAction("Undo"){
            viewModel.insertResult(viewModel.undo.value?.getContentIfNotHandled() as ScanResult)
        }.show()
    }

    inner class ItemTouchCallBack : ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(0, ItemTouchHelper.RIGHT)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val result = (viewHolder as HistoryAdapter.HistoryViewHolder).getScanResult()
            viewModel.deleteResult(result)
            showSnackBar(Event(R.string.konfirmasi_hapus))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}