package com.example.malaysiasafe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class SafetyInformationActivity extends AppCompatActivity {

   Button downloadButton, heavyRain, drought, flood, wind;
    private static final String TAG = "SafetyInfoApp";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_information);

        downloadButton = findViewById(R.id.downloadButton);
        heavyRain = findViewById(R.id.heavyRainButton);
        drought = findViewById(R.id.droughtButton);
        flood = findViewById(R.id.floodButton);
        wind = findViewById(R.id.windButton);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadAndOpenPDF();

            }
        });
        heavyRain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                downloadHeavyRain();
            }
        });
        drought.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                downloadDrought();
            }
        });
        flood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                downloadFlood();
            }
        });
        wind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                downloadWind();
            }
        });
    }



    private void downloadAndOpenPDF() {
        try {
            // Save PDF file in the app's internal storage
            InputStream inputStream = getResources().openRawResource(R.raw.landslide_info);
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "landslide_info.pdf");

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            // Use FileProvider to get a content URI for the file
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            // Open the PDF file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Grant permissions for other apps to access the file
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open PDF with"));

        } catch (Exception e) {
            Log.e(TAG, "Error saving or opening the PDF file", e);
            Toast.makeText(this, "Failed to download or open the PDF", Toast.LENGTH_SHORT).show();
        }
    }
    private void downloadDrought() {
        try {
            // Save PDF file in the app's internal storage
            InputStream inputStream = getResources().openRawResource(R.raw.drought_info);
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "drought_info.pdf");

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            // Use FileProvider to get a content URI for the file
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            // Open the PDF file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Grant permissions for other apps to access the file
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open PDF with"));

        } catch (Exception e) {
            Log.e(TAG, "Error saving or opening the PDF file", e);
            Toast.makeText(this, "Failed to download or open the PDF", Toast.LENGTH_SHORT).show();
        }
    }
    private void downloadFlood() {
        try {
            // Save PDF file in the app's internal storage
            InputStream inputStream = getResources().openRawResource(R.raw.flood_info);
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "flood_info.pdf");

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            // Use FileProvider to get a content URI for the file
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            // Open the PDF file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Grant permissions for other apps to access the file
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open PDF with"));

        } catch (Exception e) {
            Log.e(TAG, "Error saving or opening the PDF file", e);
            Toast.makeText(this, "Failed to download or open the PDF", Toast.LENGTH_SHORT).show();
        }
    }
    private void downloadWind() {
        try {
            // Save PDF file in the app's internal storage
            InputStream inputStream = getResources().openRawResource(R.raw.wind_info);
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "wind.pdf");

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            // Use FileProvider to get a content URI for the file
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            // Open the PDF file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Grant permissions for other apps to access the file
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open PDF with"));

        } catch (Exception e) {
            Log.e(TAG, "Error saving or opening the PDF file", e);
            Toast.makeText(this, "Failed to download or open the PDF", Toast.LENGTH_SHORT).show();
        }
    }
    private void downloadHeavyRain() {
        try {
            // Save PDF file in the app's internal storage
            InputStream inputStream = getResources().openRawResource(R.raw.heavy_rain_info);
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "heavy_rain_info.pdf");

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            // Use FileProvider to get a content URI for the file
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            // Open the PDF file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Grant permissions for other apps to access the file
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open PDF with"));

        } catch (Exception e) {
            Log.e(TAG, "Error saving or opening the PDF file", e);
            Toast.makeText(this, "Failed to download or open the PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
