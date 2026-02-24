package com.project.safebite.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.project.safebite.R;
import com.project.safebite.utils.UIUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScanFragment extends Fragment {

    Context context;
    View parent;
    MaterialButton btnScan;
    TextInputEditText etBarcode;
    TextView tvName, tvBrand, tvAllergens;
    ImageView ivImage;
    private static final int CAMERA_PERMISSION_CODE = 100;

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

    public void startScanner(){
        UIUtil.showSnackbar(parent, "Access Granted");
    }

    private void initializeViews(View view){
        context = requireContext();
        btnScan = view.findViewById(R.id.btnScan);
        etBarcode = view.findViewById(R.id.etBarcode);
        tvName = view.findViewById(R.id.tvName);
        tvBrand = view.findViewById(R.id.tvBrand);
        tvAllergens = view.findViewById(R.id.tvAllergens);
        ivImage = view.findViewById(R.id.ivImage);

        btnScan.setOnClickListener(v -> askCameraPermission());

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

    private void updateUI(String imageUrl, String name, String brand, List<String> allergens){
        Glide.with(parent)
                .load(imageUrl)
                .into(ivImage);

        tvName.setText(name);
        tvBrand.setText(brand);
        tvAllergens.setText(android.text.TextUtils.join("\n", allergens));
    }
    private void searchProduct(String barcode) {

        RequestQueue r = Volley.newRequestQueue(context);

        String url = "https://world.openfoodfacts.net/api/v2/product/"+barcode+"?fields=product_name,brands,image_url,ingredients_text,allergens_tags,nutrition_grades,categories_tags,nutriments";

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
                        JSONArray allergenArray = product.optJSONArray("allergens_tags");

                        List<String> allergies = new ArrayList<>();
                        if(allergenArray != null){
                            for (int i = 0; i < allergenArray.length(); i++){
                                allergies.add(allergenArray.getString(i));
                            }
                        }

                        updateUI(image, productName, brands, allergies);

                    }
                    catch (Exception e){
                        throw new RuntimeException("Failed Fetching Data");
                    }
                },
                        error -> {
                    UIUtil.showSnackbar(parent, "Error: "+ error);
        });

        r.add(jsonObjectRequest);
    }

}