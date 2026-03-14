package com.example.newsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = requireContext().getSharedPreferences("AppSettings", getContext().MODE_PRIVATE);

        // Cấu hình Google Sign-In Client để có thể đăng xuất
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Switch switchDarkMode = view.findViewById(R.id.switchDarkMode);
        Switch switchShowImage = view.findViewById(R.id.switchShowImage);
        RadioGroup radioGroupListSize = view.findViewById(R.id.radioGroupListSize);
        RadioGroup radioGroupLanguage = view.findViewById(R.id.radioGroupLanguage);
        SeekBar seekBarTextSize = view.findViewById(R.id.seekBarTextSize);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        loadSettings(switchDarkMode, switchShowImage, radioGroupListSize, radioGroupLanguage, seekBarTextSize);

        // Thiết lập listener cho các thành phần UI
        setupListeners(switchDarkMode, switchShowImage, radioGroupListSize, radioGroupLanguage, seekBarTextSize, btnLogout);
    }

    private void setupListeners(Switch switchDarkMode, Switch switchShowImage, RadioGroup radioGroupListSize, RadioGroup radioGroupLanguage, SeekBar seekBarTextSize, Button btnLogout) {
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        });

        switchShowImage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("show_image", isChecked).apply();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateAdapterSettings();
            }
        });

        radioGroupListSize.setOnCheckedChangeListener((group, checkedId) -> {
            String mode = (checkedId == R.id.radioLarge) ? "large" : "small";
            prefs.edit().putString("list_size", mode).apply();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateAdapterSettings();
            }
        });

        radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String lang = (checkedId == R.id.radioVietnamese) ? "vi" : "en";
            prefs.edit().putString("language", lang).apply();
            Toast.makeText(getContext(), "Thay đổi ngôn ngữ sẽ áp dụng sau khi khởi động lại ứng dụng", Toast.LENGTH_SHORT).show();
        });

        seekBarTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("text_size", progress).apply();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateAdapterSettings();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> signOut())
                .setNegativeButton("Hủy", null)
                .show();
        });
    }

    private void signOut() {
        // 1. Đăng xuất khỏi Google Sign-In Client
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            // 2. Đăng xuất khỏi Firebase Auth (sau khi đăng xuất Google thành công)
            FirebaseAuth.getInstance().signOut();

            // 3. Điều hướng về màn hình đăng nhập
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void loadSettings(Switch switchDarkMode, Switch switchShowImage, RadioGroup radioGroupListSize, RadioGroup radioGroupLanguage, SeekBar seekBarTextSize) {
        switchDarkMode.setChecked(prefs.getBoolean("dark_mode", false));
        switchShowImage.setChecked(prefs.getBoolean("show_image", true));

        String listSize = prefs.getString("list_size", "large");
        radioGroupListSize.check(listSize.equals("large") ? R.id.radioLarge : R.id.radioSmall);

        String lang = prefs.getString("language", "vi");
        radioGroupLanguage.check(lang.equals("vi") ? R.id.radioVietnamese : R.id.radioEnglish);

        seekBarTextSize.setProgress(prefs.getInt("text_size", 14));
    }
}
