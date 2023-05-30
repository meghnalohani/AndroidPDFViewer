package com.meghna.project.simplewebview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private EditText editText;
    private Button button;
    private WebView webView;
    private Button clearButton;
    private PDFView pdfView;

    private static String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyPermissions (Activity activity){
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        System.out.println("Write permission "+writePermission);
        System.out.println("Read permission "+readPermission);

        if(writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS,
                    REQUEST_CODE
            );
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.urlInput);
        button = findViewById(R.id.goButton);
        webView = findViewById(R.id.webView);
        clearButton = findViewById(R.id.clearButton);
        pdfView = findViewById(R.id.pdfView);

        webView.setWebViewClient(new WebViewClient());

        verifyPermissions(this);

        //WITHOUT PDFView (only WebView)
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String url = editText.getText().toString();
//                if (!url.isEmpty()) {
//                    webView.loadUrl(url);
//                }
//            }
//        });


        //WITH PDFView and WebView both
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = editText.getText().toString();
                //get the url and check if url ends wit .pdf
                if(url.endsWith(".pdf")){
                    webView.setVisibility(View.GONE);
                    pdfView.setVisibility(View.VISIBLE);
                    getPDFandStorePDF();
                    readPdf();
                    //displayPdf(url);
                }
                else {
                    pdfView.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    webView.loadUrl(url);
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
                webView.loadUrl("about:blank");
            }
        });




    }

    private byte[] getPdfFileContent() {
        String pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";
        try {
            URL url = new URL(pdfUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up connection parameters, such as request method, headers, etc.
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Authorization", "Bearer your_access_token");

            // Set other connection properties
            connection.setConnectTimeout(5000); // Timeout in milliseconds
            connection.setReadTimeout(5000); // Timeout in milliseconds


            // Check if the response code is HTTP_OK before reading the content
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                return readBytesFromStream(inputStream);

                // Store or use the pdfFileContent as needed
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        byte[] bytes = new byte[0];
        return bytes;
    }
    private void getPDFandStorePDF(){
        String fileName = "my_file.pdf";
        byte[] pdfFileContent = getPdfFileContent();

        try {
            FileOutputStream fos = new FileOutputStream(new File(getApplicationContext().getFilesDir(), fileName));
            fos.write(pdfFileContent);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void readPdf() {
        String fileName = "my_file.pdf";
        File file = new File(getApplicationContext().getFilesDir(), fileName);
        pdfView.fromFile(file)
                .load();

    }
    private byte[] readBytesFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, bytesRead);
        }

        return byteBuffer.toByteArray();
    }

    private void displayPdf(String url) {

            if (url.contains("http")) {
                //this is a web url
                Uri uri = Uri.parse(url);
                pdfView.fromUri(uri)
                        .defaultPage(0)
                        .onPageChange(new OnPageChangeListener() {
                            @Override
                            public void onPageChanged(int page, int pageCount) {
                                // Handle page change events if needed
                            }
                        })
                        .onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                // Called when the PDF has finished loading
                            }
                        })
                        .load();
            } else {
                //load from local storage

                File file = new File(url);
                if (file.exists()) {
                    // Load the PDF file
                    //load from local storage
                    pdfView.fromFile(file)
                            .defaultPage(0)
                            .onPageChange(new OnPageChangeListener() {
                                @Override
                                public void onPageChanged(int page, int pageCount) {
                                    // Handle page change events if needed
                                }
                            })
                            .enableSwipe(true)
                            .swipeHorizontal(false)
                            .onLoad(new OnLoadCompleteListener() {
                                @Override
                                public void loadComplete(int nbPages) {
                                    // Called when the PDF has finished loading
                                }
                            })
                            .load();
                } else {
                    // Show an error or handle the case when the file does not exist
                    Toast.makeText(getApplicationContext(), "file does not exist at this location", Toast.LENGTH_SHORT).show();
                }

            }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                System.out.println("Permission present");
                return;

            } else {
                // Permission denied, handle the error or inform the user
                Toast.makeText(getApplicationContext(), "" +
                        "Permission is denied", Toast.LENGTH_LONG).show();

            }
        }
    }


}
