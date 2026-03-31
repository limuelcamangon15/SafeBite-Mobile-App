package com.project.safebite.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.safebite.R;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.User;
import com.project.safebite.ui.activity.AboutActivity;
import com.project.safebite.ui.activity.LoginActivity;
import com.project.safebite.ui.activity.TermsAndConditionsActivity;
import com.project.safebite.ui.activity.WebViewActivity;
import com.project.safebite.utils.UIUtil;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private FirebaseAuth auth;
    private TextInputEditText etFullName;

    private TextView tvEmail;
    private MaterialButton btnEdit, btnSave, btnAbout, btnTermsAndConditions, btnVisitWebsite, btnLogOut;

    private MaterialCheckBox cbMilk, cbEggs, cbPeanuts, cbTreeNuts,
            cbSoy, cbWheat, cbFish, cbShellfish, cbSesame;

    private MaterialCardView mcvAbout, mcvTermsAndConditions, mcvVisitWebsite;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL);

        // databaseurl/users/uid
        ref = database.getReference("users").child(auth.getCurrentUser().getUid());

        etFullName = view.findViewById(R.id.etFullName);
        tvEmail = view.findViewById(R.id.tvEmail);

        btnEdit = view.findViewById(R.id.btnEdit);
        btnSave = view.findViewById(R.id.btnSave);
        btnLogOut = view.findViewById(R.id.btnLogOut);

        cbMilk = view.findViewById(R.id.cbMilk);
        cbEggs = view.findViewById(R.id.cbEggs);
        cbPeanuts = view.findViewById(R.id.cbPeanuts);
        cbTreeNuts = view.findViewById(R.id.cbTreeNuts);
        cbSoy = view.findViewById(R.id.cbSoybeans);
        cbWheat = view.findViewById(R.id.cbWheat);
        cbFish = view.findViewById(R.id.cbFish);
        cbShellfish = view.findViewById(R.id.cbShellfish);
        cbSesame = view.findViewById(R.id.cbSesame);

        mcvAbout = view.findViewById(R.id.mcvAbout);
        mcvTermsAndConditions = view.findViewById(R.id.mcvTermsAndConditions);
        mcvVisitWebsite = view.findViewById(R.id.mcvVisitWebsite);

        btnAbout = view.findViewById(R.id.btnAbout);
        btnTermsAndConditions = view.findViewById(R.id.btnTermsAndConditions);
        btnVisitWebsite = view.findViewById(R.id.btnVisitWebsite);

        // Load profile
        loadProfileData();

        btnEdit.setOnClickListener(v -> enableEditing());
        btnSave.setOnClickListener(v -> saveProfile());
        btnLogOut.setOnClickListener(v -> logOut());

        mcvAbout.setOnClickListener(v -> displayAbout());
        mcvTermsAndConditions.setOnClickListener(v -> displayTermsAndConditions());
        mcvVisitWebsite.setOnClickListener(v -> displayWebsite());

        btnAbout.setOnClickListener(v -> displayAbout());
        btnTermsAndConditions.setOnClickListener(v -> displayTermsAndConditions());
        btnVisitWebsite.setOnClickListener(v -> displayWebsite());
    }

    private void loadProfileData() {

        ref.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DataSnapshot snapshot = task.getResult();

                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);

                    if(user != null){
                        etFullName.setText(user.getFullName());
                        tvEmail.setText(user.getEmail());

                    }
                    else{
                        etFullName.setText("----------");
                        tvEmail.setText("----------");
                    }

                    // load allergies
                    DataSnapshot allergiesSnapshot = snapshot.child("allergies");

                    if(allergiesSnapshot.exists()){
                        List<String> allergies = new ArrayList<>();

                        for(DataSnapshot child : allergiesSnapshot.getChildren()){
                            String allergy = child.getValue(String.class);
                            allergies.add(allergy);
                        }

                        //render to the ui
                        setAllergyCheckboxes(allergies);
                    }
                }
                else{
                    UIUtil.showSnackbar(requireView(), "User data not found.");
                }
            }
            else{
                UIUtil.showSnackbar(requireView(), "Failed to read data.");
            }
        });

        // Disable editing initially
        setCheckboxesEnabled(false);
        etFullName.setEnabled(false);
    }

    private void enableEditing() {
        etFullName.setEnabled(true);
        setCheckboxesEnabled(true);

        btnEdit.setVisibility(View.GONE);
        btnSave.setVisibility(View.VISIBLE);
    }

    private void setCheckboxesEnabled(boolean enabled) {
        cbMilk.setEnabled(enabled);
        cbEggs.setEnabled(enabled);
        cbPeanuts.setEnabled(enabled);
        cbTreeNuts.setEnabled(enabled);
        cbSoy.setEnabled(enabled);
        cbWheat.setEnabled(enabled);
        cbFish.setEnabled(enabled);
        cbShellfish.setEnabled(enabled);
        cbSesame.setEnabled(enabled);
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString();

        List<String> selectedAllergens = new ArrayList<>();

        if (cbMilk.isChecked()) selectedAllergens.add("Milk");
        if (cbEggs.isChecked()) selectedAllergens.add("Eggs");
        if (cbPeanuts.isChecked()) selectedAllergens.add("Peanuts");
        if (cbTreeNuts.isChecked()) selectedAllergens.add("Tree Nuts");
        if (cbSoy.isChecked()) selectedAllergens.add("Soybeans");
        if (cbWheat.isChecked()) selectedAllergens.add("Wheat / Gluten");
        if (cbFish.isChecked()) selectedAllergens.add("Fish");
        if (cbShellfish.isChecked()) selectedAllergens.add("Shellfish");
        if (cbSesame.isChecked()) selectedAllergens.add("Sesame");

        String uid = auth.getCurrentUser().getUid();


        // databaseurl/users/uid
        DatabaseReference ref = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference("users")
                        .child(uid);


        if(!fullName.trim().equals("")) {
            // ref/fullName
            ref.child("fullName").setValue(fullName);
        }

        // ref/allergies
        ref.child("allergies").setValue(selectedAllergens)
                .addOnCompleteListener(task -> {
                    UIUtil.showSnackbar(requireView(), "Changes saved successfully.");
                })
                .addOnFailureListener(e -> {
                    UIUtil.showSnackbar(requireView(), "Failed to save changes.");
                });


        // Disable editing after save
        etFullName.setEnabled(false);
        setCheckboxesEnabled(false);

        btnSave.setVisibility(View.GONE);
        btnEdit.setVisibility(View.VISIBLE);
    }

    private void logOut(){
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        auth.signOut();

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void setAllergyCheckboxes(List<String> allergies) {

        cbMilk.setChecked(allergies.contains("Milk"));
        cbEggs.setChecked(allergies.contains("Eggs"));
        cbPeanuts.setChecked(allergies.contains("Peanuts"));
        cbTreeNuts.setChecked(allergies.contains("Tree Nuts"));
        cbSoy.setChecked(allergies.contains("Soybeans"));
        cbWheat.setChecked(allergies.contains("Wheat / Gluten"));
        cbFish.setChecked(allergies.contains("Fish"));
        cbShellfish.setChecked(allergies.contains("Shellfish"));
        cbSesame.setChecked(allergies.contains("Sesame"));
    }

    private void displayWebsite(){
        Intent intent = new Intent(requireActivity(), WebViewActivity.class);
        startActivity(intent);
    }

    private void displayTermsAndConditions(){
        Intent intent = new Intent(requireActivity(), TermsAndConditionsActivity.class);
        startActivity(intent);
    }

    private void displayAbout(){
        Intent intent = new Intent(requireActivity(), AboutActivity.class);
        startActivity(intent);
    }
}