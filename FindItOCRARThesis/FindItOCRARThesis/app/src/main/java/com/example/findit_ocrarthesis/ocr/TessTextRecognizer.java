package com.example.findit_ocrarthesis.ocr;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class TessTextRecognizer {

    private final TessBaseAPI mTess;
    private final String TAG = "Exception";

    public TessTextRecognizer(Context context, String language) {
        mTess = new TessBaseAPI();
        boolean fileExistFlag = false;

        AssetManager assetManager = context.getAssets();

        String dstPathDir = "/tesseract/tessdata/";

        String srcFile = "ell.traineddata";
        InputStream inFile = null;

        dstPathDir = context.getFilesDir() + dstPathDir;
        String dstInitPathDir = context.getFilesDir() + "/tesseract";
        String dstPathFile = dstPathDir + srcFile;
        FileOutputStream outFile = null;

        try {
            inFile = assetManager.open(srcFile);

            File f = new File(dstPathDir);

            if (!f.exists()) {
                if (!f.mkdirs()) {
                    Toast.makeText(context, srcFile + " can't be created.", Toast.LENGTH_SHORT).show();
                }
                outFile = new FileOutputStream(new File(dstPathFile));
            } else {
                fileExistFlag = true;
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());

        } finally {

            if (fileExistFlag) {
                try {
                    if (inFile != null) inFile.close();
                    mTess.init(dstInitPathDir, language);
                    return;

                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }

            if (inFile != null && outFile != null) {
                try {
                    //copy file
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inFile.read(buf)) != -1) {
                        outFile.write(buf, 0, len);
                    }
                    inFile.close();
                    outFile.close();
                    mTess.init(dstInitPathDir, language);
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            } else {
                Toast.makeText(context, srcFile + " can't be read.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public TessTextRecognizer() {

        mTess = null;
    }

    public String getOCRResult(Bitmap bitmap) {
        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩαβγδεζηθικλμνξοπρστυφχψως" +
                "ϊάόέώίύήΆΌΈΏΊΎΪ ");
        mTess.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{;:\"\\|~,./<>?0123456789" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");

        mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT_OSD);
        mTess.setImage(bitmap);

        return mTess.getUTF8Text();
    }

}