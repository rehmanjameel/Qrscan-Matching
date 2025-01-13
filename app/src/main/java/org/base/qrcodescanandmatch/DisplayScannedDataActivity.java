package org.base.qrcodescanandmatch;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.base.qrcodescanandmatch.databinding.ActivityDisplayScannedDataBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DisplayScannedDataActivity extends AppCompatActivity {

    private ActivityDisplayScannedDataBinding binding;
    private Boolean isCartonScanned = false;
    private Boolean isMinusTag = false;
    private Boolean isGoodTag = false;
    private Boolean isAddGoodTag = false;
    private Boolean isAddMinusTag = false;
    private String goodCTNR, goodPartNR, goodDNR, goodQTY, minusCTNR, minusPartNR,
            minusDNR, minusQTY, cartonCTNR, cartonPartNR, cartonDNR, cartonQTY, addMinusCTNR, addMinusPartNR,
    addMinusDNR, addMinusQTY, addGoodCTNR, addGoodDNR, addGoodPartNr, addGoodTQTY;

    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDisplayScannedDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cartonLabelBT.setOnClickListener(v -> {
            isCartonScanned = true;
            isMinusTag = false;
            isGoodTag = false;
            openScanner();
        });

        binding.minusTagBT.setOnClickListener(v -> {
            isMinusTag = true;
            isGoodTag = false;
            isCartonScanned = false;
            openScanner();
        });

        binding.goodTagBT.setOnClickListener(v -> {
            isGoodTag = true;
            isCartonScanned = false;
            isMinusTag = false;
            openScanner();
        });

        binding.backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.saveBT.setOnClickListener(v -> {

            // Check and request permission
            checkAndRequestPermission();

        });

        binding.addMinusBT.setOnClickListener(v -> {
            isAddMinusTag = true;
            isAddGoodTag = false;
            openScanner();
        });

        binding.addGoodBT.setOnClickListener(v -> {
            isAddGoodTag = true;
            isAddMinusTag = false;
            openScanner();
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        saveToExcel(goodCTNR, goodPartNR, goodDNR, goodQTY,
                                minusCTNR, minusPartNR, minusDNR, minusQTY,
                                cartonCTNR, cartonPartNR, cartonDNR, cartonQTY);
                        clearAllScreen();
                    } else {
                        Toast.makeText(this, "Permission denied. Cannot save file.", Toast.LENGTH_SHORT).show();
                    }
                });

        binding.logoutBT.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.clearBT.setOnClickListener(v -> {
            clearAllScreen();
        });

    }

    private void openScanner() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a QR Code");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setCaptureActivity(CaptureActivity.class); // Use default capture activity
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // Split the text using the delimiter
                String[] parts = intentResult.getContents().split("%3B");
                // if the intentResult is not null we'll set
                // the content and format of scan message
                if (parts.length == 4) {

                    if (isCartonScanned) {
                        binding.cartonCTNR.setText(parts[0]);
                        binding.cartonPartNR.setText(parts[1]);
                        binding.cartonDNR.setText(parts[2]);
                        binding.cartonQTY.setText(parts[3]);
                        isCartonScanned = false;
                        binding.cartonLabelBT.setClickable(false);
                        binding.minusTagBT.setClickable(true);
                        binding.minusTagBT.setEnabled(true);
                        binding.goodTagBT.setClickable(false);

                        // reset the field bg color after match fail
                        resetFieldsColor();

                    } else if (isMinusTag) {
                        binding.minusCTNR.setText(parts[0]);
                        binding.minusPartNR.setText(parts[1]);
                        binding.minusDNR.setText(parts[2]);
                        binding.minusQTY.setText(parts[3]);
                        isMinusTag = false;
                        matchCartonAndMinus();

                    } else if (isGoodTag) {
                        binding.goodCTNR.setText(parts[0]);
                        binding.goodPartNR.setText(parts[1]);
                        binding.goodDNR.setText(parts[2]);
                        binding.goodQTY.setText(parts[3]);
                        isGoodTag = false;
                        binding.goodTagBT.setClickable(false);
                        binding.minusTagBT.setClickable(false);
                        binding.cartonLabelBT.setClickable(true);

                        matchValues();

                    } else if (isAddMinusTag) {
                        binding.addMinusLayout.setVisibility(View.VISIBLE);
                        binding.addMinusCtNr.setText(parts[0]);
                        binding.addminusPartNr.setText(parts[1]);
                        binding.addMinusDnr.setText(parts[2]);
                        binding.addMinusQty.setText(parts[3]);

                        extractCartonMinusGoodTextValues();
                        int addMinusVals = Integer.parseInt(minusQTY) + Integer.parseInt(addMinusQTY);
                        Toast.makeText(this, "values added: " + addMinusVals, Toast.LENGTH_SHORT).show();

                    } else if (isAddGoodTag) {
                        binding.addGoodLayout.setVisibility(View.VISIBLE);
                        binding.addGoodCtNr.setText(parts[0]);
                        binding.addGoodPartNr.setText(parts[1]);
                        binding.addGoodDnr.setText(parts[2]);
                        binding.addGoodQty.setText(parts[3]);

                        extractCartonMinusGoodTextValues();
                        int addMinusVals = Integer.parseInt(goodQTY) + Integer.parseInt(addGoodTQTY);
                        Toast.makeText(this, "values added: " + addMinusVals, Toast.LENGTH_SHORT).show();

                    }
                }

                Log.e("barcode content", intentResult.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void extractCartonMinusGoodTextValues () {
        // Retrieve the values from the TextViews
        goodCTNR = binding.goodCTNR.getText().toString();
        goodPartNR = binding.goodPartNR.getText().toString();
        goodDNR = binding.goodDNR.getText().toString();
        goodQTY = binding.goodQTY.getText().toString();

        minusCTNR = binding.minusCTNR.getText().toString();
        minusPartNR = binding.minusPartNR.getText().toString();
        minusDNR = binding.minusDNR.getText().toString();
        minusQTY = binding.minusQTY.getText().toString();


        cartonCTNR = binding.cartonCTNR.getText().toString();
        cartonPartNR = binding.cartonPartNR.getText().toString();
        cartonDNR = binding.cartonDNR.getText().toString();
        cartonQTY = binding.cartonQTY.getText().toString();


        addGoodCTNR = binding.addGoodCtNr.getText().toString();
        addGoodPartNr = binding.addGoodPartNr.getText().toString();
        addGoodDNR = binding.addGoodDnr.getText().toString();
        addGoodTQTY = binding.addGoodQty.getText().toString();

        addMinusCTNR = binding.addMinusCtNr.getText().toString();
        addMinusPartNR = binding.addminusPartNr.getText().toString();
        addMinusDNR = binding.addMinusDnr.getText().toString();
        addMinusQTY = binding.addMinusQty.getText().toString();
    }

    private void matchCartonAndMinus() {
       extractCartonMinusGoodTextValues();

        boolean ctnrMatch = cartonCTNR.equals(minusCTNR);
        boolean partNRMatch = cartonPartNR.equals(minusPartNR);
        boolean dnrMatch = cartonDNR.equals(minusDNR);
        boolean qtyMatch = cartonQTY.equals(minusQTY);

        if (ctnrMatch && partNRMatch && dnrMatch && qtyMatch) {
            binding.matchOrNot.setText("All values match!");
            binding.matchOrNot.setVisibility(View.VISIBLE);

            binding.minusTagBT.setClickable(false);
            binding.cartonLabelBT.setClickable(false);
            binding.goodTagBT.setClickable(true);
            binding.goodTagBT.setEnabled(true);
        } else {

            // Some values do not match
            String mismatchMessage = "";

            if (!ctnrMatch) {
                mismatchMessage += "CTNR values do not match.\n";
                binding.minusCTNR.setBackgroundTintList(ColorStateList.
                        valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            if (!partNRMatch) {
                mismatchMessage += "PartNR values do not match.\n";
                binding.minusPartNR.setBackgroundTintList(ColorStateList.
                        valueOf(ContextCompat.getColor(this, R.color.red)));
                notMatchPopUp("Not Match", mismatchMessage.trim());

            }
            if (!dnrMatch) {
                mismatchMessage += "DNR values do not match.\n";
                binding.minusDNR.setBackgroundTintList(ColorStateList.
                        valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            if (!qtyMatch) {
                binding.goodTagBT.setClickable(true);

                binding.cartonLabelBT.setClickable(true);
                binding.minusTagBT.setClickable(false);
                binding.addMinusBT.setClickable(true);
                binding.goodTagBT.setClickable(true);
                binding.goodTagBT.setEnabled(true);
                binding.addMinusBT.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.
                        getColor(this, R.color.colorAccent)));

                mismatchMessage += "M-Qty values do not match.\n";
                binding.minusQTY.setBackgroundTintList(ColorStateList.
                        valueOf(ContextCompat.getColor(this, R.color.red)));
            }

            binding.matchOrNot.setVisibility(View.VISIBLE);
            binding.matchOrNot.setText(mismatchMessage.trim());
            binding.matchOrNot.setTextColor(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.red)));
        }

    }

    private void matchValues() {

        extractCartonMinusGoodTextValues();

        // Compare the values
        boolean ctnrMatch = goodCTNR.equals(minusCTNR) && goodCTNR.equals(cartonCTNR);
        boolean partNRMatch = goodPartNR.equals(minusPartNR) && goodPartNR.equals(cartonPartNR);
        boolean dnrMatch = goodDNR.equals(minusDNR) && goodDNR.equals(cartonDNR);
        boolean qtyMatch = goodQTY.equals(minusQTY) && goodQTY.equals(cartonQTY);

        // Determine the status and display it
        if (ctnrMatch && partNRMatch && dnrMatch && qtyMatch) {
            // All values match
            Toast.makeText(this, "All values match!", Toast.LENGTH_SHORT).show();
            binding.matchOrNot.setText("All values match!");
            binding.saveBT.setClickable(true);
            binding.saveBT.setEnabled(true);
            binding.matchOrNot.setVisibility(View.VISIBLE);

        } else {
            // Some values do not match
            String mismatchMessage = "";

            if (!ctnrMatch) {
                mismatchMessage += "CTNR values do not match.\n";
                binding.goodCTNR.setBackgroundTintList(ColorStateList.
                        valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            if (!partNRMatch) {
                mismatchMessage += "PartNR values do not match.\n";
                binding.goodPartNR.setBackgroundTintList(ColorStateList.
                        valueOf(ContextCompat.getColor(this, R.color.red)));
                notMatchPopUp("Not Match", mismatchMessage.trim());
            }
            if (!dnrMatch) {
                binding.goodDNR.setBackgroundTintList(ColorStateList.
                        valueOf(ContextCompat.getColor(this, R.color.red)));
                mismatchMessage += "DNR values do not match.\n";
            }
            if (!qtyMatch) {
                binding.saveBT.setEnabled(true);
                binding.saveBT.setClickable(true);
                mismatchMessage += "Qty values do not match.\n";
                binding.goodQTY.setBackgroundTintList(ColorStateList.
                        valueOf(ContextCompat.getColor(this, R.color.red)));
            }

            binding.matchOrNot.setVisibility(View.VISIBLE);
            binding.matchOrNot.setText(mismatchMessage.trim());
            binding.addGoodBT.setClickable(true);
            binding.addGoodBT.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.colorAccent)));
            binding.addMinusBT.setClickable(false);
            binding.matchOrNot.setTextColor(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.red)));

            Toast.makeText(this, mismatchMessage.trim(), Toast.LENGTH_SHORT).show();
        }

    }

    private void saveToExcel(String goodCTNR, String goodPartNR, String goodDNR, String goodQTY,
                             String minusCTNR, String minusPartNR, String minusDNR, String minusQTY,
                             String cartonCTNR, String cartonPartNR, String cartonDNR, String cartonQTY) {
        // Create a workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Matched Data");

        // Create a header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(1).setCellValue("CTNR");
        headerRow.createCell(2).setCellValue("PartNR");
        headerRow.createCell(3).setCellValue("DNR");
        headerRow.createCell(4).setCellValue("QTY");

        // Add rows for each match
        int rowIndex = 1;

        Row goodRow = sheet.createRow(rowIndex++);
        goodRow.createCell(0).setCellValue("Good Tag");
        goodRow.createCell(1).setCellValue(goodCTNR);
        goodRow.createCell(2).setCellValue(goodPartNR);
        goodRow.createCell(3).setCellValue(goodDNR);
        goodRow.createCell(4).setCellValue(goodQTY);

        Row minusRow = sheet.createRow(rowIndex++);
        minusRow.createCell(0).setCellValue("Minus Tag");
        minusRow.createCell(1).setCellValue(minusCTNR);
        minusRow.createCell(2).setCellValue(minusPartNR);
        minusRow.createCell(3).setCellValue(minusDNR);
        minusRow.createCell(4).setCellValue(minusQTY);

        Row cartonRow = sheet.createRow(rowIndex++);
        cartonRow.createCell(0).setCellValue("Carton Label");
        cartonRow.createCell(1).setCellValue(cartonCTNR);
        cartonRow.createCell(2).setCellValue(cartonPartNR);
        cartonRow.createCell(3).setCellValue(cartonDNR);
        cartonRow.createCell(4).setCellValue(cartonQTY);

        // Save the file to the Downloads directory
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDirectory, "MatchedData.xlsx");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
            workbook.close();
            Toast.makeText(this, "Data saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save Excel file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+ (Scoped Storage) no permission is required
            saveToExcel(goodCTNR, goodPartNR, goodDNR, goodQTY,
                    minusCTNR, minusPartNR, minusDNR, minusQTY,
                    cartonCTNR, cartonPartNR, cartonDNR, cartonQTY);
            clearAllScreen();
        } else {
            // For below Android 11, request WRITE_EXTERNAL_STORAGE permission
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                saveToExcel(goodCTNR, goodPartNR, goodDNR, goodQTY,
                        minusCTNR, minusPartNR, minusDNR, minusQTY,
                        cartonCTNR, cartonPartNR, cartonDNR, cartonQTY);
                clearAllScreen();
            } else {
                permissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void clearAllScreen() {
        binding.cartonCTNR.setText("Ct.Nr");
        binding.cartonPartNR.setText("Part.Nr");
        binding.cartonDNR.setText("D-Nr");
        binding.cartonQTY.setText("Qty");

        binding.minusQTY.setText("M-Qty");
        binding.minusDNR.setText("D-Nr");
        binding.minusCTNR.setText("Ct.Nr");
        binding.minusPartNR.setText("Part.Nr");

        binding.goodQTY.setText("Qty");
        binding.goodDNR.setText("D-Nr");
        binding.goodCTNR.setText("Ct.Nr");
        binding.goodPartNR.setText("Part.Nr");

        binding.addGoodQty.setText("Qty");
        binding.addGoodDnr.setText("D-Nr");
        binding.addGoodCtNr.setText("Ct.Nr");
        binding.addGoodPartNr.setText("Part.Nr");

        binding.addMinusQty.setText("Qty");
        binding.addMinusDnr.setText("D-Nr");
        binding.addMinusCtNr.setText("Ct.Nr");
        binding.addminusPartNr.setText("Part.Nr");

        binding.matchOrNot.setVisibility(View.INVISIBLE);
        binding.addMinusLayout.setVisibility(View.INVISIBLE);
        binding.addGoodLayout.setVisibility(View.INVISIBLE);

        binding.cartonLabelBT.setClickable(true);
        binding.minusTagBT.setClickable(false);
        binding.goodTagBT.setClickable(false);

        binding.addGoodBT.setClickable(false);
        binding.addMinusBT.setClickable(false);
        binding.saveBT.setClickable(false);


        resetFieldsColor();
    }

    private void resetFieldsColor() {
        binding.cartonCTNR.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));
        binding.cartonPartNR.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));
        binding.cartonDNR.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));
        binding.cartonQTY.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));
        binding.minusCTNR.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));
        binding.minusPartNR.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));
        binding.minusDNR.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));
        binding.minusQTY.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));
        binding.goodCTNR.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));
        binding.goodPartNR.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));
        binding.goodDNR.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));
        binding.goodQTY.setBackgroundTintList(ColorStateList.
                valueOf(ContextCompat.getColor(this, R.color.grey)));

        binding.addMinusBT.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.
                getColor(this, R.color.dark_grey)));

        binding.addGoodBT.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.
                getColor(this, R.color.dark_grey)));
    }

    private void notMatchPopUp(String title, String message) {
        new MaterialAlertDialogBuilder(DisplayScannedDataActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        binding.cartonLabelBT.setClickable(true);
                        binding.minusTagBT.setClickable(false);
                        binding.goodTagBT.setClickable(false);
                    }
                })
                .show();
    }

}