package com.project.safebite.ui.fragment;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import androidx.lifecycle.ViewModelProvider;
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
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.project.safebite.R;
import com.project.safebite.adapters.RecommendedProductAdapter;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.NetworkViewModel;
import com.project.safebite.model.Product;
import com.project.safebite.offlineAuth.AuthStorage;
import com.project.safebite.ui.activity.AboutActivity;
import com.project.safebite.ui.activity.PostFormActivity;
import com.project.safebite.utils.FuncUtil;
import com.project.safebite.utils.UIUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {

    Context context;
    View parent, vLine;
    MaterialButton btnScan, btnPost;
    TextInputEditText etBarcode;
    TextView tvName, tvBrand, tvAllergens, tvNutrimentsAnalysis, tvAltProducts, tvNoAlt;
    ImageView ivImage ;
    ImageButton ibSave, ibAllergic;
    PreviewView pvScanner;
    BarcodeScanner barcodeScanner;
    RecyclerView rvProduct;
    FirebaseAuth auth;
    LinearLayout bannerAllergic;
    private RecommendedProductAdapter rpAdapter;
    private ProcessCameraProvider cameraProvider;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private List<Product> recommendations = new ArrayList<>();
    private boolean isProductSaved = false;
    private boolean isAllergic = false;
    private String uid = null;
    String name="", brand="", nutrimentAnalysis="", imageUrl="", nutriscoreGrade = "";
    List <String> allergenList = null;
    List<String> userAllergies = new ArrayList<>();
    AuthStorage offlineAuth;
    NetworkViewModel networkViewModel;
    boolean isWifiConnected;
    String category = null;

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

        networkViewModel = new ViewModelProvider(requireActivity()).get(NetworkViewModel.class);
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
        offlineAuth = new AuthStorage(context);
        auth = FirebaseAuth.getInstance();
        rvProduct = view.findViewById(R.id.rvProduct);
        btnScan = view.findViewById(R.id.btnScan);
        etBarcode = view.findViewById(R.id.etBarcode);
        tvName = view.findViewById(R.id.tvName);
        tvBrand = view.findViewById(R.id.tvBrand);
        tvAllergens = view.findViewById(R.id.tvAllergens);
        tvNutrimentsAnalysis = view.findViewById(R.id.tvNutrimentsAnalysis);
        tvAltProducts = view.findViewById(R.id.tvAltProducts);
        ivImage = view.findViewById(R.id.ivImage);
        ibSave  = view.findViewById(R.id.ibSave);
        ibAllergic = view.findViewById(R.id.ibAllergic);
        pvScanner = view.findViewById(R.id.pvScanner);
        btnPost = view.findViewById(R.id.btnPost);
        bannerAllergic = view.findViewById(R.id.bannerAllergic);
        vLine = view.findViewById(R.id.vLine);
        tvNoAlt = view.findViewById(R.id.tvNoAlt);

        if (FuncUtil.isConnected(context)) {
            uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : offlineAuth.getUserId();
        } else {
            uid = offlineAuth.getUserId();
        }

        networkViewModel.getIsConnected().observe(getViewLifecycleOwner(), isConnected -> {
            uid = isConnected && auth.getCurrentUser() != null
                    ? auth.getCurrentUser().getUid()
                    : offlineAuth.getUserId();
            isWifiConnected = isConnected;
        });


        initializeBarcodeScanner();

        String path = "users/" + uid + "/allergies";
        DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(path);
        userRef.keepSynced(true);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userAllergies.clear();
                for(DataSnapshot allergenData : snapshot.getChildren()){
                    String allergen = allergenData.getValue(String.class);
                    if(allergen!=null)userAllergies.add(allergen);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnScan.setOnClickListener(v -> askCameraPermission());
        btnPost.setOnClickListener(v -> openPostForm());
        ibSave.setOnClickListener(v -> saveScan());
        ibAllergic.setOnClickListener(v-> flagAsAllergic());

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

            if(name.isEmpty() || brand.isEmpty() || allergenList.isEmpty() || imageUrl.isEmpty()){

                UIUtil.showSnackbar(parent, "All fields are required!");

            }else if(!isWifiConnected){

                UIUtil.showSnackbar(parent, "Posting not available in offline mode");

            }else{

                Bundle bundle = new Bundle();

                bundle.putString("name",name);
                bundle.putString("brand",brand);
                bundle.putString("allergens",android.text.TextUtils.join("\n", allergenList));
                bundle.putString("imageUrl",imageUrl);
                bundle.putString("source", "scan");

                Intent intent = new Intent(requireActivity(), PostFormActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);

            }

    }

    private void updateUI(String imageUrl, String name, String brand, List<String> allergens, String barcode, String nutrimentAnalysis){
        Glide.with(parent)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .override(300,300)
                .into(ivImage);

        ibSave.setImageResource(R.drawable.bookmark_24px);
        ibSave.setBackgroundResource(R.drawable.rounded_bg_green);
        ibAllergic.setBackgroundResource(R.drawable.rounded_bg_red);
        ibAllergic.setImageResource(R.drawable.warning_24px);
        ibAllergic.setColorFilter(Color.parseColor("#E24B4A"));

        tvName.setText(name);
        tvBrand.setText(brand);
        tvAllergens.setText(android.text.TextUtils.join("\n", allergens));
        tvNutrimentsAnalysis.setText(nutrimentAnalysis);
        ibSave.setVisibility(View.VISIBLE);
        ibAllergic.setVisibility(View.VISIBLE);
        vLine.setVisibility(View.VISIBLE);

        String savedPath = "users/" + uid + "/savedProducts";
        String allergicPath = "users/" + uid + "/savedAllergicProducts";
        DatabaseReference savedRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(savedPath);
        DatabaseReference allergicRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(allergicPath);

        Task<DataSnapshot> savedTask = savedRef.child(barcode).get();
        Task<DataSnapshot> allergicTask = allergicRef.child(barcode).get();

        Tasks.whenAllSuccess(savedTask, allergicTask)
                .addOnSuccessListener(results ->{
                    DataSnapshot savedSnapshot = (DataSnapshot) results.get(0);
                    DataSnapshot allergicSnapshot = (DataSnapshot) results.get(1);

                    isProductSaved = savedSnapshot.exists();
                    if(isProductSaved){
                        ibSave.setImageResource(R.drawable.bookmark_check_24px);
                        ibSave.setBackgroundResource(R.drawable.rounded_bg_green_active);
                    }else{
                        ibSave.setImageResource(R.drawable.bookmark_24px);
                        ibSave.setBackgroundResource(R.drawable.rounded_bg_green);
                    }

                    isAllergic = allergicSnapshot.exists();
                    if(isAllergic){
                        ibAllergic.setImageResource(R.drawable.warning_24px);
                        ibAllergic.setColorFilter(Color.parseColor("#E24B4A"));
                        ibAllergic.setBackgroundResource(R.drawable.rounded_bg_red_active);
                        bannerAllergic.setVisibility(View.VISIBLE);
                    }else{
                        ibAllergic.setImageResource(R.drawable.warning_24px);
                        ibAllergic.setColorFilter(Color.parseColor("#E24B4A"));
                        ibAllergic.setBackgroundResource(R.drawable.rounded_bg_red);
                        bannerAllergic.setVisibility(View.GONE);
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e("ProductCheck", "Failed to fetch product states", e);
                });


        if(!isWifiConnected){
            tvAltProducts.setVisibility(View.GONE);
            rvProduct.setVisibility(View.GONE);
        }else{
            tvAltProducts.setVisibility(View.VISIBLE);
            rvProduct.setVisibility(View.VISIBLE);
        }
    }

    private void checkAllergens(List<String> productAllergens, List<String> userAllergies){
        tvAllergens.setTextColor(ContextCompat.getColor(context, R.color.white));
        if(!productAllergens.isEmpty() && !userAllergies.isEmpty()){
            boolean allergenMatched = !Collections.disjoint(
                    userAllergies.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toList()),
                    productAllergens.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toList()));
            if(allergenMatched){
                Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
                    new MaterialAlertDialogBuilder(context)
                            .setTitle("Warning")
                            .setMessage("This Product contains allergens that are harmful to you!")
                            .setNegativeButton("Okay", null)
                            .show();

                    tvAllergens.setTextColor(ContextCompat.getColor(context, R.color.red));
                } else {
                    vibrator.vibrate(1500); // for older devices
                }

            }
        }
    }


    private void fetchProductFromApi(String barcode){
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
                        String nutritionDataPer = product.optString("nutrition_data_per", "nutrition data not available");
                        JSONObject nutrimentsObj = product.optJSONObject("nutriments");
                        JSONObject nutrimentsEstimatedObj = product.optJSONObject("nutriments_estimated");
                        JSONArray allergenArray = product.optJSONArray("allergens_tags");
                        JSONArray categoriesArray = product.optJSONArray("categories_tags");

                        List<String> allergies = new ArrayList<>();
                        if(allergenArray != null){
                            for (int i = 0; i < allergenArray.length(); i++){
                                String item = allergenArray.getString(i).substring(3, allergenArray.getString(i).length());

                                allergies.add(item);
                            }
                        }

                        if(allergies.isEmpty()){
                            allergies.add("No Allergens Listed");
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

                        String nutrimentsValue = "";
                        if (nutrimentsObj != null) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("nutriments:");
                            Iterator<String> keys = nutrimentsObj.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                if (!key.endsWith("_100g") && !key.endsWith("_unit") && !key.endsWith("_value") && !key.endsWith("_modifier") && !key.endsWith("_serving")) {
                                    sb.append(key).append(": ").append(nutrimentsObj.opt(key)).append(", ");
                                }
                            }
                            nutrimentsValue += sb.toString();
                        }

                        if (nutrimentsEstimatedObj != null) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("\nestimated nutriments:");
                            Iterator<String> keys = nutrimentsEstimatedObj.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                sb.append(key.replace("_100g", "")).append(": ").append(nutrimentsEstimatedObj.opt(key)).append(", ");
                            }
                            nutrimentsValue += sb.toString();
                        }

                        nutrimentsValue += "\nnutrition data per: " + nutritionDataPer;

                        name = productName;
                        brand = brands;
                        //allergens = allergies.toString();
                        imageUrl = image;
                        nutriscoreGrade = nutriScoreGrade;
                        allergenList = allergies;

                        checkAllergens(allergenList, userAllergies);
                        recordScan(name, brand);
                        updateUI(image, productName, brands, allergies, etBarcode.getText().toString(), "Analyzing...");
                        generateNutriAnalysis(userAllergies, allergenList, nutrimentsValue, name);
                        if(category!=null && nutriScoreGrade != null || !nutriScoreGrade.isEmpty()){
                            Log.d("CATEGORY", category);
                            if(!recommendations.isEmpty()){
                                recommendations.clear();
                            }
                            fetchAlternatives(category, nutriScoreGrade, allergies);
                        }

                    }
                    catch (Exception e){
                        e.printStackTrace();
                        Log.e("SearchProduct", "Error: " + e.getMessage());
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

        Log.d("Scan", "fetchfromapi");
        r.add(jsonObjectRequest);
    }


    private void searchProduct(String barcode) {

        String path = "users/" + uid + "/savedProducts/"+ barcode ;
        DatabaseReference productRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(path);

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            name = product.getName();
                            brand = product.getBrand();
                            imageUrl = product.getImageUrl();
                            nutriscoreGrade = product.getScore();
                            allergenList = product.getAllergens();
                            nutrimentAnalysis = product.getNutrimentsAnalysis();
                            category = product.getCategory();
                            updateUI(imageUrl, name, brand, allergenList, barcode, nutrimentAnalysis);
                            checkAllergens(allergenList, userAllergies);
                            recordScan(name, brand);
                            Log.d("Scan", "fetchfromoffline");
                            if(isWifiConnected){
                                if(category != null && nutriscoreGrade != null && !nutriscoreGrade.isEmpty()){
                                    Log.d("CATEGORY", category);
                                    if(!recommendations.isEmpty()){
                                        recommendations.clear();
                                    }
                                    fetchAlternatives(category, nutriscoreGrade, allergenList);
                                    Log.d("Scan", "fetchfromonline");
                                }
                            }
                        }
                }
                else{
                    if(isWifiConnected){
                        fetchProductFromApi(barcode);
                    }else{
                        UIUtil.showSnackbar(parent, "Product Not found in offline cache");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if(isWifiConnected){
                    fetchProductFromApi(barcode);
                }else{
                    UIUtil.showSnackbar(parent, "Unable to load product offline");
                }
            }
        });

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
         userRef.keepSynced(true);
    }


    private void saveScan(){
        isProductSaved = !isProductSaved;

        String path = "users/" + uid + "/savedProducts";
        String barcode = etBarcode.getText().toString();
        String nutrimentAnalysis = tvNutrimentsAnalysis.getText().toString();

        DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(path);

        if(isProductSaved){

            Product savedProduct = new Product(
                    imageUrl,
                    name,
                    brand,
                    allergenList,
                    barcode,
                    nutriscoreGrade,
                    System.currentTimeMillis(),
                    nutrimentAnalysis,
                    category
            );

            userRef.child(barcode).setValue(savedProduct);
            UIUtil.showSnackbar(parent, "Product Saved!");
            ibSave.setImageResource(R.drawable.bookmark_check_24px);
            ibSave.setColorFilter(Color.parseColor("#639922"));
            ibSave.setBackgroundResource(R.drawable.rounded_bg_green_active);
        }
        else{
            userRef.child(barcode).removeValue();
            UIUtil.showSnackbar(parent, "Product Unsaved!");
            ibSave.setImageResource(R.drawable.bookmark_24px);
            ibSave.setColorFilter(Color.parseColor("#639922"));
            ibSave.setBackgroundResource(R.drawable.rounded_bg_green);
        }

        userRef.keepSynced(true);

    }


    private void flagAsAllergic(){
        isAllergic = !isAllergic;

        String path = "users/" + uid + "/savedAllergicProducts";
        String barcode = etBarcode.getText().toString();
        String nutrimentAnalysis = tvNutrimentsAnalysis.getText().toString();

        DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(path);

        if(isAllergic){

            Product savedProduct = new Product(
                    imageUrl,
                    name,
                    brand,
                    allergenList,
                    barcode,
                    nutriscoreGrade,
                    System.currentTimeMillis(),
                    nutrimentAnalysis,
                    category
            );

            userRef.child(barcode).setValue(savedProduct);

            ibAllergic.setImageResource(R.drawable.warning_24px);
            ibAllergic.setColorFilter(Color.parseColor("#E24B4A"));
            ibAllergic.setBackgroundResource(R.drawable.rounded_bg_red_active);
            bannerAllergic.setVisibility(View.VISIBLE);
            UIUtil.showSnackbar(parent, "Product Marked as Allergic!");
        }else{
            userRef.child(barcode).removeValue();

            ibAllergic.setImageResource(R.drawable.warning_24px);
            ibAllergic.setColorFilter(Color.parseColor("#E24B4A"));
            ibAllergic.setBackgroundResource(R.drawable.rounded_bg_red);
            bannerAllergic.setVisibility(View.GONE);
            UIUtil.showSnackbar(parent, "Product Unmarked as Allergic!");
        }

        userRef.keepSynced(true);
    }


    private void fetchAlternatives(String category, String originalScore, List<String> userAllergens){
        Log.d("HELLO", "im here");
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
                            Log.d("Nutri score", score);
                            Log.d("HELLO", "im here2");

                            List<String> productAllergens = new ArrayList<>();
                            if(allergensArray != null){
                                for(int j = 0; j < allergensArray.length(); j++){
                                    String a = allergensArray.getString(j).substring(3, allergensArray.getString(j).length());
                                    productAllergens.add(a);
                                }
                            }

                            if (productAllergens.isEmpty()) {
                                productAllergens.add("No Allergens Listed");
                            }

                            if(score != null && !score.isEmpty() && isBetterNutriScore(score, originalScore)){
                                recommendations.add(new Product (image, name, brand, productAllergens, barcode ,score, System.currentTimeMillis(), "Analysis not available", category));
                            }
                        }

                        Collections.sort(recommendations, (a, b) ->
                                nutriScoreValue(b.getScore()) - nutriScoreValue(a.getScore())
                        );

                        if(recommendations.size() > 10){
                            recommendations = recommendations.subList(0, 10);
                        }
                        Log.d("HELLO", "hello: " + recommendations.size());
                        updateRecommendationsUI(recommendations);

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        UIUtil.showSnackbar(parent, "Check your internet connection");
                    } else if (error instanceof AuthFailureError) {
                        UIUtil.showSnackbar(parent, "Authentication failed");
                    } else if (error instanceof ServerError) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            UIUtil.showSnackbar(parent, "Product not found");
                            return;
                        }
                        UIUtil.showSnackbar(parent, "Server error, please try again later");
                    } else if (error instanceof Network) {
                        UIUtil.showSnackbar(parent, "Network error, please try again");
                    } else if (error instanceof ParseError) {
                        UIUtil.showSnackbar(parent, "Failed to read server response");
                    } else {
                        UIUtil.showSnackbar(parent, "Unexpected error: " + error.toString());
                    }
                });

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

    private void generateNutriAnalysis(List<String> userAllergens, List<String> productAllergens, String productNutriments, String productName){

        String url = "https://safebiteapi.vercel.app/api/nutrimentsAnalysis";

        RequestQueue r = Volley.newRequestQueue(context);

        try {

            JSONObject body = new JSONObject();
            body.put("productNutriments", productNutriments.toString());
            body.put("productAllergens", productAllergens.toString());
            body.put("userAllergens", userAllergens);
            body.put("productName", productName);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        try {

                             String reply = response.getString("reply");
                             reply = reply.replace("*", "");
                             tvNutrimentsAnalysis.setText(reply);

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        UIUtil.showSnackbar(parent, "Failed to fetch recommendations");
                    });

            r.add(request);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private boolean isBetterNutriScore(String candidate, String original){
        return nutriScoreValue(candidate) >= nutriScoreValue(original);
    }

    private void updateRecommendationsUI(List<Product> recommendations){


        if(recommendations.isEmpty()){
            rvProduct.setVisibility(View.GONE);
            tvNoAlt.setVisibility(View.VISIBLE);
            tvNoAlt.setText("No Alternative Products to Show");
        }else{
            tvNoAlt.setVisibility(View.GONE);
            rvProduct.setVisibility(View.VISIBLE);
            rvProduct.setLayoutManager(new LinearLayoutManager(context));
            rvProduct.setHasFixedSize(true);
            rvProduct.setNestedScrollingEnabled(false);

            rpAdapter = new RecommendedProductAdapter(parent, context, recommendations, userAllergies);
            rvProduct.setAdapter(rpAdapter);
        }

    }

}


