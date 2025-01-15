package org.base.qrcodescanandmatch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.base.qrcodescanandmatch.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActivityResultLauncher<String> permissionLauncher;
    String userName, password;

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
//            Intent intent = new Intent(this, DisplayScannedDataActivity.class);
//            startActivity(intent);
            openScanner();
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {

                    } else {
                        Toast.makeText(this, "Permission denied. Cannot save file.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openScanner() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan user Login");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setCaptureActivity(CaptureActivity.class); // Use default capture activity
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // Split the text using the delimiter
                String name = intentResult.getContents();
                Toast.makeText(this, "user name: " + name, Toast.LENGTH_SHORT).show();
                binding.userNameET.setText(name);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void verifyFields() {
        userName = binding.userNameET.getText().toString();
        password = binding.passwordET.getText().toString();

        if (!userName.isEmpty() && !password.isEmpty()) {
//            Intent intent = new Intent(this, DisplayScannedDataActivity.class);
//            startActivity(intent);
            checkAndRequestPermission();
        } else {
            Toast.makeText(this, "Please enter the valid data!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogin(String username) {
        // Define app-specific folder
        File appDirectory = new File(Environment.getExternalStorageDirectory(), "QRCodeMatch");
        if (!appDirectory.exists()) {
            boolean isCreated = appDirectory.mkdirs(); // Create the folder if it doesn't exist
            if (!isCreated) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to create app folder", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Define file location in the app folder
        File file = new File(appDirectory, "Users.xlsx");

        Workbook workbook;
        Sheet sheet;

        // Check if file exists
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheet("Users");
                if (sheet == null) {
                    sheet = workbook.createSheet("Users");
                }
            } catch (IOException e) {
                e.printStackTrace();
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error reading file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Users");
            // Create header row
            Row headerRow = sheet.createRow(0);
            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Username");
        }

        boolean userExists = false;

        // Check if username already exists
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(0);
                if (cell != null && username.equals(cell.getStringCellValue())) {
                    userExists = true;
                    break;
                }
            }
        }

        if (userExists) {
            Intent intent = new Intent(this, DisplayScannedDataActivity.class);
            intent.putExtra("user_name", username);
            startActivity(intent);
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Login Successful: User already exists", Toast.LENGTH_SHORT).show();

        } else {
            // Add new user to the Excel file
            int newRowIdx = sheet.getLastRowNum() + 1;
            Row newRow = sheet.createRow(newRowIdx);
            Cell newCell = newRow.createCell(0);
            newCell.setCellValue(username);

            // Save the file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
                workbook.close();
                binding.progressBar.setVisibility(View.GONE);

                Toast.makeText(this, "New user added. Login Successful!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+ (Scoped Storage) no permission is required
            // For Android 11+ (Scoped Storage)
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                binding.progressBar.setVisibility(View.VISIBLE);

                handleLogin(userName);
            }
        } else {
            // For below Android 11, request WRITE_EXTERNAL_STORAGE permission
            // For Android 10 and below
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                binding.progressBar.setVisibility(View.VISIBLE);
                handleLogin(userName);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }

        }
    }

}