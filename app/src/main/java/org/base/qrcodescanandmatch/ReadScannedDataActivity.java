package org.base.qrcodescanandmatch;

import android.os.Bundle;
import android.os.Environment;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.base.qrcodescanandmatch.databinding.ActivityReadScannedDataBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadScannedDataActivity extends AppCompatActivity {

    private ActivityReadScannedDataBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadScannedDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ScanData> scanDataList = readExcelFile();
        ScanDataAdapter adapter = new ScanDataAdapter(scanDataList);
        binding.recyclerView.setAdapter(adapter);

    }

    private List<ScanData> readExcelFile() {
        List<ScanData> scanDataList = new ArrayList<>();
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDirectory, "MatchedData.xlsx");

        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Workbook workbook = new XSSFWorkbook(fis);
                Sheet sheet = workbook.getSheet("Matched Data");

                for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Skip header row
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        String timestamp = row.getCell(0).getStringCellValue();
                        String tagType = row.getCell(1).getStringCellValue();
                        String ctnr = row.getCell(2).getStringCellValue();
                        String partNr = row.getCell(3).getStringCellValue();
                        String dnr = row.getCell(4).getStringCellValue();
                        String qty = row.getCell(5).getStringCellValue();

                        ScanData data = new ScanData(timestamp, tagType, ctnr, partNr, dnr, qty);
                        scanDataList.add(data);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return scanDataList;
    }

}