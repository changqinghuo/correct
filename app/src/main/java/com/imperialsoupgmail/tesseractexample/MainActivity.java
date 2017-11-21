package com.imperialsoupgmail.tesseractexample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Bitmap image;
    private TessBaseAPI mTess;
    String datapath = "";
    private static final int STORAGE=1;
    private String mFilePath;
    private static int CAMERA_CODE1 = 1;
    private static int CAMERA_CODE2 = 2;
    private ImageView mImageViewShow;
    private String m_sOCRresult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageViewShow = (ImageView)findViewById(R.id.imageView);
        //init image
        image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);

        //initialize Tesseract API
        String language = "eng";
        datapath = getFilesDir()+ "/tesseract/";
        mTess = new TessBaseAPI();

        checkFile(new File(datapath + "tessdata/"));

        mTess.init(datapath, language);

        String tempPath = Environment.getExternalStorageDirectory().getPath();

        mFilePath = tempPath + "/" + "test1.png";
    }

    public void processImage(View view){
        checkPermission();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoUri = Uri.fromFile(new File(mFilePath));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, CAMERA_CODE1);
    }
    public void processOCR(){
        final ProgressDialog progress = ProgressDialog.show(this, "Loading", "Parsing result...", true);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                m_sOCRresult = "NOT MATCH";

                mTess.setImage(image);
                m_sOCRresult = mTess.getUTF8Text();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
                        OCRTextView.setText(m_sOCRresult);
                        progress.dismiss();

                    }
                });

            }

        });


    }

    private void checkPermission() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
     /*   if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        */
        if (!permissions.isEmpty()) {
            Toast.makeText(this, "Storage access needed to manage the picture.", Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, STORAGE);
        } else { // We already have permissions, so handle as normal
           // takePicture();
        }
    }

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
                copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {


            if (requestCode == CAMERA_CODE1) {
                /**
                 * 通过暂存路径取得图片
                 */
                FileInputStream fis = null;
                Bitmap bitmap = null;
                try {
                    fis = new FileInputStream(mFilePath);
                    bitmap = BitmapFactory.decodeStream(fis);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                image = bitmap;
                mImageViewShow.setImageBitmap(bitmap);

                processOCR();

            } else if (requestCode == CAMERA_CODE2) {
                /**
                 * 通过data取得图片
                 */
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                mImageViewShow.setImageBitmap(bitmap);
            }

        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
