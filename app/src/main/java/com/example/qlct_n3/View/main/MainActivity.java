package com.example.qlct_n3.View.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.qlct_n3.R;
import com.example.qlct_n3.base.DataBaseManager;
import com.example.qlct_n3.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DataBaseManager.getInstance(getApplicationContext());

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewpager.setAdapter(adapter);

        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.page_1) {
                    binding.viewpager.setCurrentItem(0);
                } else if (item.getItemId() == R.id.page_2) {
                    binding.viewpager.setCurrentItem(1);
                } else if (item.getItemId() == R.id.page_3) {
                    binding.viewpager.setCurrentItem(2);
                } else if (item.getItemId() == R.id.page_4) {
                    binding.viewpager.setCurrentItem(3);
                }
                return true;
            }
        });

        binding.viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        binding.bottomNavigation.getMenu().findItem(R.id.page_1).setChecked(true);
                        break;
                    case 1:
                        binding.bottomNavigation.getMenu().findItem(R.id.page_2).setChecked(true);
                        break;
                    case 2:
                        binding.bottomNavigation.getMenu().findItem(R.id.page_3).setChecked(true);
                        break;
                    case 3:
                        binding.bottomNavigation.getMenu().findItem(R.id.page_4).setChecked(true);
                        break;
                }
            }
        });
    }
}