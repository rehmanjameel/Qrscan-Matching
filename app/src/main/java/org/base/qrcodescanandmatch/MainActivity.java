package org.base.qrcodescanandmatch;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.base.qrcodescanandmatch.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.loginButton.setOnClickListener(v -> {
            verifyFields();
        });

        binding.scanQr.setOnClickListener(view -> {
            Intent intent = new Intent(this, DisplayScannedDataActivity.class);
            startActivity(intent);
        });
    }

    private void verifyFields() {
        String userName = binding.userNameET.getText().toString();
        String password = binding.passwordET.getText().toString();

        if (userName.equals("test") && password.equals("1234")) {
            Intent intent = new Intent(this, DisplayScannedDataActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please enter the valid data!", Toast.LENGTH_SHORT).show();
        }
    }
}