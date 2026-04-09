package com.project.safebite.ui.fragment;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Network;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.project.safebite.R;
import com.project.safebite.adapters.RecommendedProductAdapter;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.Product;
import com.project.safebite.ui.activity.AboutActivity;
import com.project.safebite.ui.activity.PostFormActivity;
import com.project.safebite.utils.UIUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {

    Context context;
    View parent;
    MaterialButton btnScan, btnPost;
    TextInputEditText etBarcode;
    TextView tvName, tvBrand, tvAllergens;
    ImageView ivImage ;
    ImageButton ibBookmark;
    PreviewView pvScanner;
    BarcodeScanner barcodeScanner;
    RecyclerView rvProduct;
    FirebaseAuth auth;
    private RecommendedProductAdapter rpAdapter;
    private ProcessCameraProvider cameraProvider;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private List<Product> recommendations = new ArrayList<>();
    private boolean isProductSaved = false;
    private String uid = null;
    String name="", brand="", allergens="", imageUrl="", nutriscoreGrade = "";
    List <String> allergenList = null;


    public ScanFragment() {
        // Required empty public constructor
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_scan, container, false);
        parent = view;

        initializeViews(view);

        return view;
    }

    private void askCameraPermission(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        else{
            startScanner();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startScanner();
            }
            else{
                UIUtil.showSnackbar(parent, "Camera permission is required");
            }
        }
    }

    private void stopScanner(){
        if(cameraProvider != null){
            cameraProvider.unbindAll();
        }
        pvScanner.setVisibility(View.GONE);
    }

    public void startScanner(){
        pvScanner.setVisibility(View.VISIBLE);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(pvScanner.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(
                        Executors.newSingleThreadExecutor(),
                        this::processImageProxy
                );

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview,imageAnalysis);
            }
            catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(ImageProxy imageProxy) {

        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {

            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            barcodeScanner.process(image).addOnSuccessListener(barcodes -> {

                        for (Barcode barcode : barcodes) {

                            String value = barcode.getRawValue();

                            if (value != null) {

                                requireActivity().runOnUiThread(() -> {
                                    etBarcode.setText(value);
                                    searchProduct(value);
                                    stopScanner();
                                });

                                imageProxy.close();
                                return; // stop after first scan
                            }
                        }
                    })
                    .addOnFailureListener(Throwable::printStackTrace)
                    .addOnCompleteListener((task) -> imageProxy.close());

        } else {
            imageProxy.close();
        }
    }

    private void initializeBarcodeScanner(){
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();

        barcodeScanner = BarcodeScanning.getClient(options);
    }

    private void initializeViews(View view){
        context = requireContext();
        auth = FirebaseAuth.getInstance();
        rvProduct = view.findViewById(R.id.rvProduct);
        btnScan = view.findViewById(R.id.btnScan);
        etBarcode = view.findViewById(R.id.etBarcode);
        tvName = view.findViewById(R.id.tvName);
        tvBrand = view.findViewById(R.id.tvBrand);
        tvAllergens = view.findViewById(R.id.tvAllergens);
        ivImage = view.findViewById(R.id.ivImage);
        ibBookmark  = view.findViewById(R.id.ibBookmark);
        pvScanner = view.findViewById(R.id.pvScanner);
        btnPost = view.findViewById(R.id.btnPost);
        uid = auth.getCurrentUser().getUid();

        initializeBarcodeScanner();



        btnScan.setOnClickListener(v -> askCameraPermission());
        btnPost.setOnClickListener(v -> openPostForm());
        ibBookmark.setOnClickListener(v -> saveScan());



        etBarcode.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
            ){
                searchProduct(etBarcode.getText().toString());

                return true;
            }
            return false;
        });
    }

    private void openPostForm(){

            if(name.isEmpty() || brand.isEmpty() || allergens.isEmpty() || imageUrl.isEmpty()){

                UIUtil.showSnackbar(parent, "All fields are required!");

            }else{

                Bundle bundle = new Bundle();

                bundle.putString("name",name);
                bundle.putString("brand",brand);
                bundle.putString("allergens",allergens);
                bundle.putString("imageUrl",imageUrl);

                Intent intent = new Intent(requireActivity(), PostFormActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);

            }

    }

    private void updateUI(String imageUrl, String name, String brand, List<String> allergens, String barcode){
        Glide.with(parent)
                .load(imageUrl)
                .into(ivImage);

        tvName.setText(name);
        tvBrand.setText(brand);
        tvAllergens.setText(android.text.TextUtils.join("\n", allergens));
        ibBookmark.setVisibility(View.VISIBLE);

        String path = "users/" + uid + "/savedProducts";
        DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(path);
        userRef.child(barcode).get().addOnSuccessListener(snapshot -> {
            if(snapshot.exists()){
                isProductSaved = true;
                ibBookmark.setBackgroundResource(R.drawable.bookmark_solid);
            } else {
                isProductSaved = false;
                ibBookmark.setBackgroundResource(R.drawable.bookmark_regular);
            }
        });

    }

    private void vibrate(List<String> userAllergens, List<String> productAllergens){
        if(!allergens.isEmpty()){
            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1500); // for older devices
            }
        }

    }
    String category = null;

    private void searchProduct(String barcode) {

        RequestQueue r = Volley.newRequestQueue(context);

        String url = "https://world.openfoodfacts.org/api/v2/product/"+barcode+"?fields=product_name,brands,image_url,ingredients_text,allergens_tags,nutriscore_grade,categories_tags,nutriments";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {

                    try {
                        int status = response.optInt("status");
                        if (status == 0) {
                            UIUtil.showSnackbar(parent, "Product not found");
                            return;
                        }

                        JSONObject product = response.optJSONObject("product");
                        if (product == null) {
                            UIUtil.showSnackbar(parent, "No product data available");
                            return;
                        }

                        String image = product.optString("image_url", "Image is not available");
                        String productName = product.optString("product_name", "Name is not available");
                        String brands = product.optString("brands", "Brand is not available");
                        String nutriScoreGrade = product.optString("nutriscore_grade", "Nutriscore not available");
                        JSONArray allergenArray = product.optJSONArray("allergens_tags");
                        JSONArray categoriesArray = product.optJSONArray("categories_tags");

                        List<String> allergies = new ArrayList<>();
                        if(allergenArray != null){
                            for (int i = 0; i < allergenArray.length(); i++){
                                String item = allergenArray.getString(i).substring(3, allergenArray.getString(i).length());

                                allergies.add(item);
                            }
                        }

                        List<String> categories = new ArrayList<>();

                        if(categoriesArray != null){
                            for(int i = 0; i < categoriesArray.length(); i++){
                                String item = categoriesArray.getString(i);

                                categories.add(item);
                            }

                            for(int i = categories.size()-1; i >= 0; i--){
                                String tag = categories.get(i);
                                if(tag.startsWith("en:")){
                                    category = tag.replace("en:", "");
                                    break;
                                }
                            }
                        }

                        if(category!=null && nutriScoreGrade != null || !nutriScoreGrade.isEmpty()){
                            Log.d("CATEGORY", category);
                            if(!recommendations.isEmpty()){
                                recommendations.clear();
                            }
                            new Handler(Looper.getMainLooper()).postDelayed(()->{
                                fetchAlternatives(category, nutriScoreGrade, allergies);
                            }, 1500);
                        }

                        name = productName;
                        brand = brands;
                        allergens = allergies.toString();
                        imageUrl = image;
                        nutriscoreGrade = nutriScoreGrade;
                        allergenList = allergies;

                        //call vibrate here
                        recordScan(name, brand);
                        updateUI(image, productName, brands, allergies, etBarcode.getText().toString());

                    }
                    catch (Exception e){
                        throw new RuntimeException("Failed Fetching Data");
                    }
                },
                error -> {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        UIUtil.showSnackbar(parent, "Check your internet connection");
                    }
                    else if (error instanceof AuthFailureError) {
                        UIUtil.showSnackbar(parent, "Authentication failed");
                    }
                    else if (error instanceof ServerError) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            UIUtil.showSnackbar(parent, "Product not found");
                            return;
                        }
                        UIUtil.showSnackbar(parent, "Server error, please try again later");
                    }
                    else if (error instanceof Network) {
                        UIUtil.showSnackbar(parent, "Network error, please try again");
                    }
                    else if (error instanceof ParseError) {
                        UIUtil.showSnackbar(parent, "Failed to read server response");
                    }
                    else {
                        UIUtil.showSnackbar(parent, "Unexpected error: " + error.toString());
                    }
        });



        r.add(jsonObjectRequest);
    }

    private void recordScan(String name, String brand){
         String path = "users/" + uid + "/scanHistory";
         String barcode = etBarcode.getText().toString();
         DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(path);

         String scanId = userRef.push().getKey();

         Product product = new Product(
                 name,
                 brand,
                 barcode,
                 System.currentTimeMillis()
         );

         userRef.child(scanId).setValue(product);
    }


    private void saveScan(){
        isProductSaved = !isProductSaved;

        String path = "users/" + uid + "/savedProducts";
        String barcode = etBarcode.getText().toString();

        DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(path);

        if(isProductSaved){

            Product savedProduct = new Product(
                    imageUrl,
                    name,
                    brand,
                    allergenList,
                    barcode,
                    nutriscoreGrade,
                    System.currentTimeMillis()
            );

            userRef.child(barcode).setValue(savedProduct);
            UIUtil.showSnackbar(parent, "Saved Product!");
            ibBookmark.setBackgroundResource(R.drawable.bookmark_solid);
        }
        else{
            userRef.child(barcode).removeValue();
            UIUtil.showSnackbar(parent, "Product Unsaved!");
            ibBookmark.setBackgroundResource(R.drawable.bookmark_regular);
        }

    }

    private void fetchAlternatives(String category, String originalScore, List<String> userAllergens){

        RequestQueue r = Volley.newRequestQueue(context);

            String url = "https://safebiteapi.vercel.app/api/recommendation?category="+category;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray products = response.optJSONArray("products");
                        if(products == null) return;

                        for(int i = 0; i < products.length(); i++){
                            JSONObject obj = products.getJSONObject(i);

                            String name = obj.optString("product_name", "");
                            String brand = obj.optString("brands", "");
                            String image = obj.optString("image_url", "");
                            String score = obj.optString("nutriscore_grade", null);
                            String barcode = obj.optString("code",null);
                            JSONArray allergensArray = obj.optJSONArray("allergens_tags");

                            List<String> productAllergens = new ArrayList<>();
                            if(allergensArray != null){
                                for(int j = 0; j < allergensArray.length(); j++){
                                    String a = allergensArray.getString(j).substring(3, allergensArray.getString(j).length());
                                    productAllergens.add(a);
                                }
                            }

                            if(score != null && isBetterNutriScore(score, originalScore)){
                                recommendations.add(new Product(image, name, brand, productAllergens, barcode ,score, System.currentTimeMillis()));
                            }
                        }

                        Collections.sort(recommendations, (a, b) ->
                                nutriScoreValue(b.getScore()) - nutriScoreValue(a.getScore())
                        );

                        if(recommendations.size() > 10){
                            recommendations = recommendations.subList(0, 10);
                        }

                        updateRecommendationsUI(recommendations);

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                },
                error -> {
                    UIUtil.showSnackbar(parent, "Failed to fetch recommendations");
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "SafeBiteApp/1.0 (safebiteproject2026@gmail.com)");
                headers.put("Cookie", "user_session&YKkbaH2m9JVXZGxU6cihlnd3jANX9RZzusvm8Jfk2KL6AOOnHVNGS0e83AOpQMDR&user_id&safebite");
                return headers;
            }
        };

        r.add(request);
    }

    private int nutriScoreValue(String score){
        if(score == null) return 0;
        switch(score.toLowerCase()){
            case "a": return 5;
            case "b": return 4;
            case "c": return 3;
            case "d": return 2;
            case "e": return 1;
            default: return 0;
        }

    }

    private boolean isBetterNutriScore(String candidate, String original){
        return nutriScoreValue(candidate) > nutriScoreValue(original);
    }

    private void updateRecommendationsUI(List<Product> recommendations){
        rvProduct.setLayoutManager(new LinearLayoutManager(context));
        rvProduct.setHasFixedSize(true);
        rvProduct.setNestedScrollingEnabled(false);

        rpAdapter = new RecommendedProductAdapter(parent, context, recommendations);
        rvProduct.setAdapter(rpAdapter);

    }

}


