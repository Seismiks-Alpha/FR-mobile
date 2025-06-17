package com.seismiks.nyamapp.ui.home

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.transition.MaterialContainerTransform
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.seismiks.nyamapp.R
import com.seismiks.nyamapp.ViewModelFactory
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.databinding.FragmentHomeBinding
import com.seismiks.nyamapp.ui.camera.CameraActivity
import com.seismiks.nyamapp.ui.heightWeight.HeightWeightSettingActivity
import com.seismiks.nyamapp.ui.history.HistoryActivity
import java.util.Calendar

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var pieChart: PieChart
    private lateinit var auth: FirebaseAuth
    private var totalCalorie: Float = 2000f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()

        auth = Firebase.auth

        binding.btnChangeFit.setOnClickListener {
            val intent = Intent(requireActivity(), HeightWeightSettingActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        setupObserver()

        fetchData()

        pieChart = binding.pieChart
        setupPieChartStyle()

        // Card click listeners
        /* binding.cardSetting.setOnClickListener {
            val intent = Intent(requireActivity(), SettingActivity::class.java)
            startActivity(intent)
        } */

        binding.cardScan.setOnClickListener {
            val intent = Intent(requireActivity(), CameraActivity::class.java)
            startActivity(intent)
        }

        binding.cardHistory.setOnClickListener {
            val intent = Intent(requireActivity(), HistoryActivity::class.java)
            startActivity(intent)
        }

        /* binding.cardChat.setOnClickListener {
            val intent = Intent(requireActivity(), ChatActivity::class.java)
            startActivity(intent)
        } */
    }

    private fun setupPieChartStyle() {
        pieChart.setUsePercentValues(true)
        pieChart.getDescription().setEnabled(false)
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.setDragDecelerationFrictionCoef(0.95f)
        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.setHoleRadius(58f)
        pieChart.setTransparentCircleRadius(61f)
        pieChart.setDrawCenterText(true)
        pieChart.setRotationAngle(0f)
        pieChart.setRotationEnabled(true)
        pieChart.setHighlightPerTapEnabled(true)
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)
    }

    private fun setupObserver() {
        // OBSERVER 1: Untuk Profile
        viewModel.profile.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    // Update UI Profile
                    binding.tvUserName.text = result.data.displayName
                    binding.tvHeightValue.text = result.data.profile?.height?.toString() ?: "-"
                    binding.tvWeightValue.text = result.data.profile?.weight?.toString() ?: "-"

                    auth.currentUser?.getIdToken(false)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val idToken = task.result.token
                            idToken?.let {
                                viewModel.getCaloriesToday(it)
                            }
                        }
                    }
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvUserName.text = "Error"
                    binding.tvHeightValue.text = "Error"
                    binding.tvWeightValue.text = "Error"
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.caloriesToday.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.pieChart.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.pieChart.visibility = View.VISIBLE

                    val consumedCalories = result.data.today.totalCalories.toFloat()
                    val targetCalories = result.data.progress.recommended.toFloat()
                    val entries = ArrayList<PieEntry>().apply {
                        add(PieEntry(consumedCalories, "Masuk"))
                        if (targetCalories > consumedCalories) {
                            add(PieEntry(targetCalories - consumedCalories, "Sisa"))
                        } else {
                            add(PieEntry(0f, "Sisa"))
                        }
                    }

                    pieChart.setEntryLabelColor(Color.WHITE)
                    pieChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD)

                    val dataSet = PieDataSet(entries, "Asupan Kalori Harian").apply {
                        colors = listOf(
                            ContextCompat.getColor(requireContext(), R.color.green),
                            ContextCompat.getColor(requireContext(), R.color.cream)
                        )
                        setDrawIcons(false)
                        sliceSpace = 3f
                        iconsOffset = MPPointF(0f, 40f)
                        selectionShift = 5f
                    }

                    val data = PieData(dataSet).apply {
                        setValueFormatter(PercentFormatter(pieChart))
                        setValueTextSize(15f)
                        setValueTypeface(Typeface.DEFAULT_BOLD)
                        setValueTextColor(Color.RED)
                    }

                    pieChart.apply {
                        this.data = data
                        highlightValues(null)
                        invalidate()
                        centerText = "${consumedCalories.toInt()} \n dari \n ${targetCalories.toInt()} Kcal"
                        setCenterTextSize(18f)
                        setCenterTextTypeface(Typeface.DEFAULT_BOLD)
                        setCenterTextColor(Color.RED)
                    }

                    if (targetCalories > consumedCalories) {
                        binding.tvWarning.visibility = View.GONE
                        binding.ivWarningLeft.visibility = View.GONE
                        binding.ivWarningRight.visibility = View.GONE
                    } else {
                        binding.tvWarning.visibility = View.VISIBLE
                        binding.ivWarningLeft.visibility = View.VISIBLE
                        binding.ivWarningRight.visibility = View.VISIBLE
                    }
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Log.e("HomeFragment", "Error caloriesToday: ${result.error}")
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun fetchData() {
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result.token
                if (idToken != null) {
                    viewModel.getProfileInfo(idToken)
                } else {
                    Log.e("HomeFragment", "ID Token is null")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
