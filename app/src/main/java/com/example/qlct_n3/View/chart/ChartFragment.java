package com.example.qlct_n3.View.chart;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qlct_n3.Model.GiaoDich;
import com.example.qlct_n3.Model.SpendingInChart;
import com.example.qlct_n3.R;
import com.example.qlct_n3.base.DataBaseManager;
import com.example.qlct_n3.databinding.FragmentChartBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class ChartFragment extends Fragment {
    private static final String TAG = "ChartFragment";
    private ChartViewModel viewModel;
    private FragmentChartBinding binding;
    private ChartAdapter adapter;
    private Calendar calendar;
    private int rv;
    private int spd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentChartBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(ChartViewModel.class);
        adapter = new ChartAdapter();
        calendar = Calendar.getInstance();
        rv = 0;
        spd = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setupPieChart();
        loadView();
        return binding.getRoot();
    }
    private void setupPieChart() {
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setExtraOffsets(5, 10, 5, 5);
        binding.pieChart.setDragDecelerationFrictionCoef(0.95f);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(Color.WHITE);
        binding.pieChart.setTransparentCircleRadius(61f);
        binding.pieChart.getLegend().setEnabled(false); // Disable the legend
    }

    private void updatePieChartData(List<SpendingInChart> data, String label) {
        if (data == null || data.isEmpty()) {
            binding.pieChart.clear();
            binding.pieChart.invalidate();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (SpendingInChart item : data) {
            entries.add(new PieEntry(Math.abs(item.getTien()), item.getTenDanhMuc()));
        }

        PieDataSet dataSet = new PieDataSet(entries, label);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(10f);
        pieData.setValueTextColor(Color.BLACK);

        binding.pieChart.setData(pieData);
        binding.pieChart.invalidate(); // refresh
    }

    // Hàm load xử lý onClick
    private void loadView() {
        binding.recyclerview.setAdapter(adapter);
        binding.tapLayout.addTab(binding.tapLayout.newTab().setText("Chi tiêu"));
        binding.tapLayout.addTab(binding.tapLayout.newTab().setText("Thu nhập"));
        createDataChart();
        binding.tvMonth.setText("Tháng " + (calendar.get(Calendar.MONTH) + 1));

        binding.tapLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        spendingAdapter();
                        break;
                    default:
                        revenueAdapter();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        spendingAdapter();
                        break;
                    default:
                        revenueAdapter();
                        break;
                }
            }
        });

        binding.imvBackNonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, -1);
                String selectedDate = String.valueOf(calendar.get(Calendar.MONTH) + 1);
                binding.tvMonth.setText("Tháng " + selectedDate);
                check();
            }
        });

        binding.imvIncreaseMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, 1);
                String selectedDate = String.valueOf(calendar.get(Calendar.MONTH) + 1);
                binding.tvMonth.setText("Tháng " + selectedDate);
                check();
            }
        });
    }

    // Hàm load recycleview chi tiêu
    private void spendingAdapter() {
        viewModel.get_SpendingInChartChi(requireContext(), calendar.get(Calendar.MONTH) + 1);
        viewModel.SpendingInChartChi().observe(getViewLifecycleOwner(),
                new Observer<List<SpendingInChart>>() {
            @Override
            public void onChanged(List<SpendingInChart> spendingInCharts) {
                List<SpendingInChart> aggregatedList = new ArrayList<>();
                spd = 0;
                if (spendingInCharts.isEmpty()) {
                    binding.tvNothing.setVisibility(View.VISIBLE);
                } else {
                    Map<String, List<SpendingInChart>> listMap = spendingInCharts.stream().
                            collect(Collectors.groupingBy(SpendingInChart::getTenDanhMuc));
                    listMap.forEach((_tenDanhMuc, list) -> {
                        SpendingInChart s = new SpendingInChart(list.stream().
                                mapToLong(SpendingInChart::getTien).sum(), _tenDanhMuc, list.get(0).
                                getIcon());
                        aggregatedList.add(s);
                        spd -= s.getTien();
                    });
                    binding.tvNothing.setVisibility(View.GONE);
                }
                adapter.setAdapter(aggregatedList);
                updatePieChartData(aggregatedList,"Chi tiêu");
                updateTotal();
            }
        });
    }

    // hàm load recycleview thông kê khoản tiêu
    private void revenueAdapter() {
        viewModel.get_SpendingInChartThu(requireContext(), calendar.get(Calendar.MONTH) + 1);
        viewModel.SpendingInChartThu().observe(getViewLifecycleOwner(),
                new Observer<List<SpendingInChart>>() {
            @Override
            public void onChanged(List<SpendingInChart> spendingInCharts) {
                List<SpendingInChart> aggregatedList = new ArrayList<>();
                rv = 0;
                if (spendingInCharts.isEmpty()) {
                    binding.tvNothing.setVisibility(View.VISIBLE);
                } else {
                    Map<String, List<SpendingInChart>> listMap = spendingInCharts.stream().
                            collect(Collectors.groupingBy(SpendingInChart::getTenDanhMuc));
                    listMap.forEach((_tenDanhMuc, list) -> {
                        SpendingInChart s = new SpendingInChart(list.stream().
                                mapToLong(SpendingInChart::getTien).sum(), _tenDanhMuc, list.get(0).
                                getIcon());
                        aggregatedList.add(s);
                        rv += s.getTien();
                    });
                    binding.tvNothing.setVisibility(View.GONE);
                }
                adapter.setAdapter(aggregatedList);
                updatePieChartData(aggregatedList,"Thu nhập");
                updateTotal();
            }
        });
    }

    @Override
    public void onResume() {
        check();
        super.onResume();
    }

    private void createDataChart() {
        revenueAdapter();
        spendingAdapter();
    }

    private void check() {
        switch (binding.tapLayout.getSelectedTabPosition()) {
            case 0:
                revenueAdapter();
                spendingAdapter();
                break;
            default:
                spendingAdapter();
                revenueAdapter();
                break;
        }
    }

    private void updateTotal() {
        binding.tvSpending.setText(spd + " đ");
        binding.tvRevenue.setText(rv + " đ");
        binding.tvTotal.setText((spd + rv) + " đ");
    }
}