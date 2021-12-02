package com.example.findit_ocrarthesis.activities;


// TODO: 9/21/2021 extract a class for the location if possible

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.ViewModelProvider;

import com.example.findit_ocrarthesis.R;
import com.example.findit_ocrarthesis.app_database.ProfessionalViewModel;
import com.example.findit_ocrarthesis.location.LocationMatcher;
import com.example.findit_ocrarthesis.ocr.ImageProcessor;
import com.example.findit_ocrarthesis.ocr.TessTextRecognizer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;

import org.opencv.android.OpenCVLoader;

import java.lang.ref.WeakReference;
import java.text.Normalizer;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.text.Html.fromHtml;
import static com.example.findit_ocrarthesis.constants.Constants.DEFAULT_LANGUAGE;

public class MainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener,
        Scene.OnUpdateListener
{

    private ProfessionalViewModel professionalViewModel;
    private FusedLocationProviderClient fusedLocationClient;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private static final String TAG = "MainActivity";
    TessTextRecognizer tessTextRecognizer;
    ImageProcessor imageProcessor;
    ProgressDialog mProgressDialog ;
    private ArFragment arFragment;
    private ViewRenderable viewRenderable;
    private TextView info_text;
    private Location current_location;
    private Anchor currentAnchor = null;
    private AnchorNode currentAnchorNode;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Node titleNode;
    private String[] PERMISSIONS;
    private boolean data_exists = false;
    private boolean on_tap_flag = false;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "INSTALLED OPEN");
        } else {
            Log.d(TAG, "NOT OPENCV");
        }
    }

    private String all_info_under_100cm;
    private String all_info_100cm;
    private Anchor anchor_plane;
    private boolean enter = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PERMISSIONS = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
        };

        if(!hasPermission(MainActivity.this, PERMISSIONS)){
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, 1);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        findLocation();

        professionalViewModel = new ViewModelProvider.AndroidViewModelFactory(MainActivity.this
                .getApplication())
                .create(ProfessionalViewModel.class);

        getLifecycle().addObserver(professionalViewModel);

        getSupportFragmentManager().addFragmentOnAttachListener(this);
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }
        loadModels();
    }

    public void loadModels() {
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title)
                .build()
                .thenAccept(viewRenderable -> {

                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }

                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onViewCreated(ArFragment arFragment, ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);
        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        config.setPlaneFindingMode(Config.PlaneFindingMode.VERTICAL);
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (viewRenderable == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }
        data_exists = false;
        imageProcessor = new ImageProcessor(arFragment /*BitmapFactory.decodeResource(getResources(), R.drawable.sample)*/);
//        Bitmap b = captureImage();/* BitmapFactory.decodeResource(getResources(), R.drawable.numsample);*/
        tessTextRecognizer = new TessTextRecognizer( this, DEFAULT_LANGUAGE);
        titleNode = getInfoNode(hitResult, viewRenderable);

        if(current_location!=null) {
            // do it in background because it can take a while
            doTasks();

        }

    }

    private Node getInfoNode(HitResult hitResult, ViewRenderable viewRenderable) {

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        Node intermediateNode = new Node();
        intermediateNode.setParent(anchorNode);
        Vector3 anchorUp = anchorNode.getUp();
        intermediateNode.setLookDirection(Vector3.up(), anchorUp);
        clearAnchor();
        currentAnchor = anchor;
        currentAnchorNode = anchorNode;
        Node infoNode = new Node();
        infoNode.setParent(intermediateNode);
        infoNode.setEnabled(false);
        infoNode.setLocalPosition(new Vector3(0.0f, 0.0f, 0.0f));
        infoNode.setLocalRotation(Quaternion.axisAngle(new Vector3(1f, 0, 0), -90f));
        infoNode.setRenderable(viewRenderable);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this);
        arFragment.getArSceneView().getScene().addChild(anchorNode);

        return infoNode;
    }

    private void clearAnchor() {
        currentAnchor = null;
        if (currentAnchorNode != null) {
            arFragment.getArSceneView().getScene().removeChild(currentAnchorNode);
            currentAnchorNode.getAnchor().detach();
            currentAnchorNode.setParent(null);
            currentAnchorNode = null;
        }
    }

    private void doTasks() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "Processing",
                    "Αναζήτηση πληροφοριών για τον συγκεκριμένο επαγγελματία...", true);
        } else {
            mProgressDialog.show();
        }
        executor.execute(() -> {
            Bitmap bitmap_processed = imageProcessor.doImageProcess(); // imageProcessing
            final String[] srcText = {tessTextRecognizer.getOCRResult(bitmap_processed)}; // ocr stuff

            handler.post(() -> {
                if (srcText[0] != null && !srcText[0].equals("")) {
                    srcText[0] = srcText[0].replaceAll("[\\t\\n\\r]+", " ");

                    srcText[0] = stripAccents(srcText[0]);

                    String[] text_array = srcText[0].toUpperCase().split(" ");
//                    Toast.makeText(this, srcText[0], Toast.LENGTH_SHORT).show();
//                    for (String text :
//                            text_array) {
//                        retrieveData(text.trim(), current_location);
//                    }
                    getProfInfo("Κωνσταντινουπόλεως 400, Άγιοι Ανάργυροι, 13562, ΑΤΤΙΚΗΣ","ΨΟΥΝΗΣ");
                } else {
                    Toast.makeText(this, "Δεν μπόρεσε να αναγνωριστεί κάποιο κείμενο", Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();
            });

        });

    }

    private String stripAccents(String s)
    {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    public void retrieveData(String srcText, Location curr_location) {
        professionalViewModel.getAllAddresses(srcText)
                .observe(MainActivity.this, addresses ->{
                    for (String strAddress:
                            addresses
                    ) {
                        LocationMatcher locationMatcher =
                                new LocationMatcher(new Geocoder(MainActivity.this, new Locale("el_GR")),
                                        strAddress);
                        if (locationMatcher.getDistanceFrom(curr_location) < 20.0){
                            Toast.makeText(this, "Αντιστοίχηση Διευθύνσεων ολοκληρώθηκε!", Toast.LENGTH_SHORT).show();
                            getProfInfo(strAddress, srcText);
                        }
                    }
                    on_tap_flag = true;
                });
    }

    private void getProfInfo(String strAddress, String name) {
        Toast.makeText(this, "Αντιστοίχηση Διευθύνσεων ολοκληρώθηκε!", Toast.LENGTH_SHORT).show();

        professionalViewModel.findAll(strAddress, name).observe(MainActivity.this, info ->{
            String[] email = info.getEmail().split(":");
            all_info_under_100cm =
                    "<b>" + "Category: " + "</b>" + info.getCategory() + "<br>"
                            + "<b>" + "Description: " + "</b>" + info.getDescription() + "<br>"
                            + "<b>" + "Office Number: " + "</b>" + info.getOfficePhone() + "<br>"
                            + "<b>" + "Mobile Phone: "+ "</b>" + info.getMobilePhone() +  "<br>"
                            + "<b>" + "Website: "+ "</b>" + info.getWebsite() + "<br>"
                            + "<b>" + email[0] + " " + "</b>" + email[1]
            ;
            all_info_100cm =
                    "<b>" + name + "<br>" + info.getProfName() + "</b>";
            titleNode.setEnabled(true);
        });
    }

    private void findLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            current_location = location;
                        }
                    }
                });
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location permission required")
                        .setMessage("Please add location permission!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the
//                    // location-related task you need to do.
//                    if (ContextCompat.checkSelfPermission(this,
//                            Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED) {
//                        //Request location updates:
//                       findLocation();
//                    }
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//        }
//    }

    private boolean hasPermission(Context context, String... PERMISSIONS){

        if(context != null && PERMISSIONS != null){

            for (String permission:
                    PERMISSIONS) {
                if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    public void onUpdate(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();
        if (viewRenderable == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentAnchorNode != null && all_info_100cm != null && all_info_under_100cm != null ) {
            float distanceMeters = distanceCamera(currentAnchor.getPose(), frame.getCamera().getPose());
            viewRenderable.setShadowCaster(false);
            viewRenderable.setShadowReceiver(false);
            info_text = (TextView) viewRenderable.getView();
            info_text.setAutoLinkMask(Linkify.ALL);
            info_text.setLinkTextColor(Color.rgb(71, 149, 255));

            if (distanceMeters < 1.0 && all_info_under_100cm != null && all_info_100cm != null) {
                info_text.setTextSize(4);
                info_text.setText(fromHtml(all_info_under_100cm));
            } else if (distanceMeters > 1.00 && all_info_under_100cm != null && all_info_100cm != null) {
                info_text.setTextSize(10);
                info_text.setText(fromHtml(all_info_100cm));
            }
        }

    }

    private float distanceCamera(Pose planePose, Pose cameraPose) {
        float dx = planePose.tx() - cameraPose.tx();
        float dy = planePose.ty() - cameraPose.ty();
        float dz = planePose.tz() - cameraPose.tz();
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }


}