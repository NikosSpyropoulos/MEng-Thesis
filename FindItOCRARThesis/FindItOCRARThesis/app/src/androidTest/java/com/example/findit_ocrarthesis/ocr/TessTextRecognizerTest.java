package com.example.findit_ocrarthesis.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;

import com.example.findit_ocrarthesis.R;
import com.example.findit_ocrarthesis.activities.MainActivity;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.example.findit_ocrarthesis.constants.Constants.DEFAULT_LANGUAGE;

public class TessTextRecognizerTest extends TestCase {


    private Context instrumentationContext;

    @Before
    public void setup() {
        instrumentationContext =  ApplicationProvider.getApplicationContext();
    }
    
    @Test
    public void getOCRResult(){

        Bitmap test_bitmap = BitmapFactory.decodeResource(getInstrumentation().getContext().getResources(), R.drawable.sample);
        ImageProcessor imageProcessor = new ImageProcessor(test_bitmap);
        Bitmap test_bitmap_processed = imageProcessor.doImageProcess();
        TessTextRecognizer tessTextRecognizer = new TessTextRecognizer(instrumentationContext, DEFAULT_LANGUAGE);

        assertEquals("ΨΟΥΝΗΣ ΚΩΝΣΤΑΝΤΙΝΟΣ", tessTextRecognizer.getOCRResult(test_bitmap_processed));

    }

}