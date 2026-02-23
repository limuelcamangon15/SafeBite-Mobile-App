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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
    private void searchProduct(String barcode) {

        RequestQueue r = Volley.newRequestQueue(context);

        String url = "https://world.openfoodfacts.net/api/v2/product/"+barcode+"?fields=product_name,brands,image_url,ingredients_text,allergens_tags,nutrition_grades,categories_tags,nutriments";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    JSONObject product = jsonObject.getJSONObject("product");

                    String productName = product.getString("product_name");
                    String brands = product.getString("brands");

                    JSONArray allergenArray = product.getJSONArray("allergens_tags");

                    ArrayList<String> allergies = new ArrayList<>();


                    for(int i = 0; i < allergenArray.length(); i++){
                        allergies.add(allergenArray.get(i).toString());
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                UIUtil.showSnackbar(parent, "Failed getting data of "+barcode);
            }
        });

        r.add(jsonObjectRequest);
    }

}