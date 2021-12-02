package com.example.findit_ocrarthesis.ocr;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;

import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.ux.ArFragment;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class ImageProcessor {
    ArFragment arFragment;
    Bitmap initial_bitmap;


    public ImageProcessor(ArFragment arFragment){
        this.arFragment = arFragment;
        this.initial_bitmap = captureImage();
//        initial_bitmap = captureImage();
    }
    public ImageProcessor(Bitmap bitmap){
        this.initial_bitmap = bitmap;
    }

    private Bitmap captureImage() {
        // TODO this is the code for the snapshot, too much noise, will use it later
        Image capturedImage;
        try {
            capturedImage = arFragment.getArSceneView().getArFrame().acquireCameraImage();
            Bitmap bitmap = imageToBitmap(capturedImage); // convert Image to Bitmap
            return rotateImage(bitmap, 90); // the image was appearing as landscape this is why the rotate Image
        } catch (NotYetAvailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap imageToBitmap(Image capturedImage) {
        ByteBuffer cameraPlaneY = capturedImage.getPlanes()[0].getBuffer();
        ByteBuffer cameraPlaneU = capturedImage.getPlanes()[1].getBuffer();
        ByteBuffer cameraPlaneV = capturedImage.getPlanes()[2].getBuffer();

        byte[] compositeByteArray = new byte[cameraPlaneY.capacity() + cameraPlaneU.capacity() + cameraPlaneV.capacity()];
        cameraPlaneY.get(compositeByteArray, 0, cameraPlaneY.capacity());
        cameraPlaneU.get(compositeByteArray, cameraPlaneY.capacity(), cameraPlaneU.capacity());
        cameraPlaneV.get(compositeByteArray, cameraPlaneY.capacity() + cameraPlaneU.capacity(), cameraPlaneV.capacity());

        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        YuvImage yuvImage = new YuvImage(compositeByteArray, ImageFormat.NV21, capturedImage.getWidth(), capturedImage.getHeight(), null);
        yuvImage.compressToJpeg( new Rect(0, 0, capturedImage.getWidth(),capturedImage.getHeight()), 75, baOutputStream);
        byte[] byteForBitmap  = baOutputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteForBitmap, 0, byteForBitmap.length);

        return bitmap;
    }

    // TODO: 9/22/2021 put them in the ImageProcessing class
    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public Bitmap doImageProcess() {
        Bitmap blackWhite_bitmap = convertToBlackWhite(initial_bitmap);
        return /*initial_bitmap*/erodeDilateImg(blackWhite_bitmap);
    }


    /**
     * Image Corrosion/Expansion Processing Corrosion and expansion are very useful for processing noise-free images, use with caution
     */
    private Bitmap erodeDilateImg(Bitmap compressImage) {
        Mat src = new Mat();
        Utils.bitmapToMat(compressImage, src);

        Mat outImage = new Mat();
        // The smaller the size, the smaller the corrosion unit, and the closer the picture is to the original picture
        Mat structImage = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));

        /**
         * Image Corrosion Description of Corrosion: A part of the image is convolved with the specified kernel, and the smallest value of the kernel is calculated and assigned to the specified area.
         * Corrosion can be understood as the'area reduction' of the'highlight area' in the image. It means that the highlighted part will be eroded away by the pixels that are not the highlighted part, making the highlighted part less and less.
         */
//        Imgproc.erode(src, outImage, structImage, new Point(-1, -1), 2);
//        src = outImage;

        /**
         * Expansion Expansion description: A part of the image is convolved with the specified kernel, and the maximum value of the kernel is calculated and assigned to the specified area.
         * Dilation can be understood as the'area expansion' of the'highlight area' in the image. It means that the highlighted part will erode the non-highlighted part, making the highlighted part more and more.
         */
        Imgproc.dilate(src, outImage, structImage, new Point(-1, -1), 1);
        src = outImage;
        Bitmap newBitmap = compressImage;
        Utils.matToBitmap(src, newBitmap);
        return newBitmap;
    }

    private Bitmap convertToBlackWhite(Bitmap compressImage)
    {
        Log.d("CV", "Before converting to black");
        Mat imageMat = new Mat();
        Utils.bitmapToMat(compressImage, imageMat);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
        Bitmap gray_bitmap = compressImage;
        Utils.matToBitmap(imageMat, gray_bitmap);
        Imgproc.GaussianBlur(imageMat, imageMat, new Size(3, 3), 0);
        Bitmap gaussian_bitmap = compressImage;
        Utils.matToBitmap(imageMat, gaussian_bitmap);
//        Imgproc.adaptiveThreshold(imageMat, imageMat, 255, ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 4);
        Imgproc.medianBlur(imageMat, imageMat, 3);
        Bitmap median_bitmap = compressImage;
        Utils.matToBitmap(imageMat, median_bitmap);
        Imgproc.threshold(imageMat, imageMat, 50, 255, Imgproc.THRESH_OTSU | THRESH_BINARY);
        Bitmap newBitmap = compressImage;
        Utils.matToBitmap(imageMat, newBitmap);
//        imageView.setImageBitmap(newBitmap);
        Log.d("CV", "After converting to black");


        return newBitmap;

    }

    private Bitmap computeSkew(Bitmap compressImage ) {


        //Load this image in grayscale
        Mat img = new Mat();
        Utils.bitmapToMat(compressImage, img);
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);

        //Binarize it
        //Use adaptive threshold if necessary
        Imgproc.adaptiveThreshold(img, img, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 40);

        //Invert the colors (because objects are represented as white pixels, and the background is represented by black pixels)
        Core.bitwise_not( img, img );
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));

        //We can now perform our erosion, we must declare our rectangle-shaped structuring element and call the erode function
        Imgproc.erode(img, img, element);

        //Find all white pixels
        Mat wLocMat = Mat.zeros(img.size(),img.type());
        Core.findNonZero(img, wLocMat);

        //Create an empty Mat and pass it to the function
        MatOfPoint matOfPoint = new MatOfPoint( wLocMat );

        //Translate MatOfPoint to MatOfPoint2f in order to user at a next step
        MatOfPoint2f mat2f = new MatOfPoint2f();
        matOfPoint.convertTo(mat2f, CvType.CV_32FC2);

        //Get rotated rect of white pixels
        RotatedRect rotatedRect = Imgproc.minAreaRect( mat2f );

        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);
        List<MatOfPoint> boxContours = new ArrayList<>();
        boxContours.add(new MatOfPoint(vertices));
        Imgproc.drawContours( img, boxContours, 0, new Scalar(128, 128, 128), -1);

        double resultAngle = rotatedRect.angle;
        if (rotatedRect.size.width > rotatedRect.size.height)
        {
            rotatedRect.angle = rotatedRect.angle < -45 ? rotatedRect.angle + 90.f : rotatedRect.angle;
        }

        //Or
        //rotatedRect.angle = rotatedRect.angle < -45 ? rotatedRect.angle + 90.f : rotatedRect.angle;
        Mat img_start = new Mat();
        Utils.bitmapToMat(compressImage, img_start);

        Mat result = deskew( img_start , resultAngle );

        Bitmap newBitmap = compressImage;
        Utils.matToBitmap(result, newBitmap);
        return newBitmap;
    }

    private Mat deskew(Mat src, double angle) {
        Point center = new Point(src.width()/2, src.height()/2);
        Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        //1.0 means 100 % scale
        Size size = new Size(src.width(), src.height());
        Imgproc.warpAffine(src, src, rotImage, size, Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS);
        return src;
    }

}
