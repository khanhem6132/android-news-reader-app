package com.example.newsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ⚙️ Mỗi lần app khởi động lại, reset cờ thông báo
        SharedPreferences prefsReset = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefsReset.edit().putBoolean("hasShownNotification", false).apply();

        // ⚙️ BƯỚC 5: Đọc chế độ sáng/tối đã lưu và áp dụng ngay khi khởi động app
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }



        // ⚙️ Sau khi set theme, mới set layout (để giao diện hiển thị đúng)
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigationView);

        // Mặc định mở Home
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_saved) {
                selected = new SavedFragment();
            } else if (id == R.id.nav_liked) {
                selected = new LikedFragment();
            } else if (id == R.id.nav_settings) {
                selected = new SettingsFragment();
            }

            if (selected != null) {
                loadFragment(selected);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    public void updateAdapterSettings() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).refreshNewsSettings();
        }
    }
}
