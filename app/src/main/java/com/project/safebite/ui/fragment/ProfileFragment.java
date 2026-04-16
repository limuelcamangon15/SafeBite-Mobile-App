package com.project.safebite.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.safebite.R;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.NetworkViewModel;
import com.project.safebite.model.User;
import com.project.safebite.offlineAuth.AuthStorage;
import com.project.safebite.ui.activity.AboutActivity;
import com.project.safebite.ui.activity.LoginActivity;
import com.project.safebite.ui.activity.TermsAndConditionsActivity;
import com.project.safebite.ui.activity.WebViewActivity;
import com.project.safebite.utils.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private FirebaseAuth auth;
    private TextInputEditText etFullName, etNewPassword, etConfirmNewPassword, etOldPassword;

    View vLine;
    private TextView tvEmail, tvChangePassword;
    private MaterialButton btnEditAllergens, btnSaveAllergens, btnAbout, btnTermsAndConditions, btnVisitWebsite, btnLogOut, btnChangePassword, btnEditProfile, btnSaveProfile;
    private TextInputLayout tilOldPassword, tilNewPassword, tilConfirmNewPassword;

    private MaterialCheckBox cbMilk, cbEggs, cbPeanuts, cbTreeNuts,
            cbSoy, cbWheat, cbFish, cbShellfish, cbSesame, cbGluten;

    private MaterialCardView mcvAbout, mcvTermsAndConditions, mcvVisitWebsite;
    private boolean isWifiConnected;
    NetworkViewModel networkViewModel;

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
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmNewPassword = view.findViewById(R.id.etConfirmNewPassword);
        etOldPassword = view.findViewById(R.id.etOldPassword);
        tilNewPassword = view.findViewById(R.id.tilNewPassword);
        tilOldPassword = view.findViewById(R.id.tilOldPassword);
        tilConfirmNewPassword = view.findViewById(R.id.tilConfirmNewPassword);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);
        vLine = view.findViewById(R.id.vLine);

        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnEditAllergens = view.findViewById(R.id.btnEditAllergens);
        btnSaveAllergens = view.findViewById(R.id.btnSaveAllergens);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
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
        cbGluten = view.findViewById(R.id.cbGluten);

        mcvAbout = view.findViewById(R.id.mcvAbout);
        mcvTermsAndConditions = view.findViewById(R.id.mcvTermsAndConditions);
        mcvVisitWebsite = view.findViewById(R.id.mcvVisitWebsite);
        networkViewModel = new ViewModelProvider(requireActivity()).get(NetworkViewModel.class);

        btnAbout = view.findViewById(R.id.btnAbout);
        btnTermsAndConditions = view.findViewById(R.id.btnTermsAndConditions);
        btnVisitWebsite = view.findViewById(R.id.btnVisitWebsite);

        // Load profile
        loadProfileData();
        setRealTimeEditTextListener();

        networkViewModel.getIsConnected().observe(getViewLifecycleOwner(), isConnected->{
            isWifiConnected = isConnected;});

        btnEditAllergens.setOnClickListener(v -> enableAllergenEditing());
        btnSaveAllergens.setOnClickListener(v -> saveAllergens());
        btnEditProfile.setOnClickListener(v-> enableProfileEditing());
        btnSaveProfile.setOnClickListener(v-> saveProfile());
        btnLogOut.setOnClickListener(v -> logOut());

        mcvAbout.setOnClickListener(v -> displayAbout());
        mcvTermsAndConditions.setOnClickListener(v -> displayTermsAndConditions());
        mcvVisitWebsite.setOnClickListener(v -> displayWebsite());

        btnAbout.setOnClickListener(v -> displayAbout());
        btnTermsAndConditions.setOnClickListener(v -> displayTermsAndConditions());
        btnVisitWebsite.setOnClickListener(v -> displayWebsite());
        btnChangePassword.setOnClickListener(v->updatePassword(etNewPassword.getText().toString(),
                etConfirmNewPassword.getText().toString(),
                etOldPassword.getText().toString()
        ));
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
        disableProfileEdit();
        disableAllergenEdit();
//        clearPasswordFields();
//        setCheckboxesEnabled(false);
//        etFullName.setEnabled(false);
//        etNewPassword.setEnabled(false);
//        etConfirmNewPassword.setEnabled(false);
//        etOldPassword.setEnabled(false);
//        tilOldPassword.setVisibility(View.GONE);
//        tilNewPassword.setVisibility(View.GONE);
//        tilConfirmNewPassword.setVisibility(View.GONE);
//        vLine.setVisibility(View.GONE);
//        tvChangePassword.setVisibility(View.GONE);
//        btnChangePassword.setVisibility(View.GONE);
    }

    private void enableProfileEditing() {
        vLine.setVisibility(View.VISIBLE);
        tvChangePassword.setVisibility(View.VISIBLE);
        tilOldPassword.setVisibility(View.VISIBLE);
        tilNewPassword.setVisibility(View.VISIBLE);
        tilConfirmNewPassword.setVisibility(View.VISIBLE);
        etFullName.setEnabled(true);
        etNewPassword.setEnabled(true);
        etOldPassword.setEnabled(true);
        etConfirmNewPassword.setEnabled(true);
        btnEditProfile.setVisibility(View.GONE);
        btnChangePassword.setVisibility(View.VISIBLE);
        btnSaveProfile.setVisibility(View.VISIBLE);
    }

    private void enableAllergenEditing(){
        btnEditAllergens.setVisibility(View.GONE);
        btnSaveAllergens.setVisibility(View.VISIBLE);
        setCheckboxesEnabled(true);
    }


    private boolean isPasswordValid(String pass, String confirmPass){
        if(pass.isEmpty() || confirmPass.isEmpty()){
            UIUtil.showSnackbar(requireView(), "Password fields cannot be empty");
            return false;
        }else if(!pass.equals(confirmPass)){
            UIUtil.showSnackbar(requireView(), "Passwords did not match");
            return false;
        }else if(pass.length() < 8 || confirmPass.length() < 8 ){
            UIUtil.showSnackbar(requireView(), "Password length must be 8 characters long");
            return false;
        }else{
            return true;
        }
    }

    private void updatePassword(String newPass, String confirmPass, String oldPassword) {
        if (oldPassword.isEmpty()) {
            UIUtil.showSnackbar(requireView(), "Please enter your current password");
            return;
        }

        if (isPasswordValid(newPass, confirmPass) && isWifiConnected) {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                reauthenticateAndUpdate(user, newPass, oldPassword);
            }
        } else if (!isWifiConnected) {
            UIUtil.showSnackbar(requireView(), "You cannot update your password in offline mode");
        }
    }

    private void clearPasswordFields(){
        etOldPassword.setText("");
        etNewPassword.setText("");
        etConfirmNewPassword.setText("");
    }

    private void reauthenticateAndUpdate(FirebaseUser user, String newPassword, String currentPassword) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        UIUtil.showSnackbar(requireView(), "Password updated successfully!");
                        disableProfileEdit();
                    } else {
                        UIUtil.showSnackbar(requireView(), "Update failed: " + updateTask.getException().getMessage());
                    }
                });
            } else {
                UIUtil.showSnackbar(requireView(), "Current password incorrect.");
            }
        });
    }

    private void setRealTimeEditTextListener(){
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();

                if(etNewPassword.hasFocus()){
                    if(input.isEmpty()){
                        tilNewPassword.setError("Password is required.");
                    }else if(input.length() < 8 ){
                        tilNewPassword.setError("Password must be at least 8 characters long.");
                    }else{
                        tilNewPassword.setError(null);
                    }
                }
            }
        });

        etConfirmNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if(etConfirmNewPassword.hasFocus()){
                    if(input.isEmpty()){
                        tilConfirmNewPassword.setError("Password is required.");
                    }else if(input.length() < 8 ){
                        tilConfirmNewPassword.setError("Password must be at least 8 characters long.");
                    }else if(!etNewPassword.getText().toString().equals(input)){
                        tilConfirmNewPassword.setError("Passwords do not match.");
                    }else{
                        tilConfirmNewPassword.setError(null);
                    }
                }
            }
        });

        etOldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if(etOldPassword.hasFocus()){
                    if(input.isEmpty()){
                        tilOldPassword.setError("Password is required.");
                    }else if(input.length() < 8 ){
                        tilOldPassword.setError("Password must be at least 8 characters long.");
                    }else{
                        tilOldPassword.setError(null);
                    }
                }
            }
        });
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
        cbGluten.setEnabled(enabled);
    }

    private void saveAllergens(){
        List<String> selectedAllergens = new ArrayList<>();

        if (cbMilk.isChecked()) selectedAllergens.add("Milk");
        if (cbEggs.isChecked()) selectedAllergens.add("Eggs");
        if (cbPeanuts.isChecked()) selectedAllergens.add("Peanuts");
        if (cbTreeNuts.isChecked()) selectedAllergens.add("Tree Nuts");
        if (cbSoy.isChecked()) selectedAllergens.add("Soybeans");
        if (cbWheat.isChecked()) selectedAllergens.add("Wheat");
        if (cbFish.isChecked()) selectedAllergens.add("Fish");
        if (cbShellfish.isChecked()) selectedAllergens.add("Shellfish");
        if (cbSesame.isChecked()) selectedAllergens.add("Sesame");
        if (cbGluten.isChecked()) selectedAllergens.add("Gluten");

        String uid = auth.getCurrentUser().getUid();

        // databaseurl/users/uid
        DatabaseReference ref = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference("users")
                .child(uid);

        ref.child("allergies").setValue(selectedAllergens)
                .addOnCompleteListener(task -> {
                    UIUtil.showSnackbar(requireView(), "Changes saved successfully.");
                })
                .addOnFailureListener(e -> {
                    UIUtil.showSnackbar(requireView(), "Failed to save changes.");
                });
        ref.child("allergies").keepSynced(true);

        disableAllergenEdit();
    }


    private void saveProfile() {
        String fullName = etFullName.getText().toString();

        String uid = auth.getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference("users")
                .child(uid);


        if(!fullName.trim().equals("")) {
            // ref/fullName
            updateNamesOnPosts(fullName, uid);
            ref.child("fullName").setValue(fullName)
                    .addOnSuccessListener(e->{ UIUtil.showSnackbar(requireView(), "Changes saved successfully.");})
                    .addOnFailureListener(e->{ UIUtil.showSnackbar(requireView(), "Failed to save changes.");});
            ref.child("fullName").keepSynced(true);
        }else{
            UIUtil.showSnackbar(requireView(), "Name cannot be empty.");
            return;
        }

        // Disable editing after save
        disableProfileEdit();
    }

    private void disableAllergenEdit(){
        setCheckboxesEnabled(false);
        btnSaveAllergens.setVisibility(View.GONE);
        btnEditAllergens.setVisibility(View.VISIBLE);
    }
    private void disableProfileEdit(){
        clearPasswordFields();
        etFullName.setEnabled(false);
        etFullName.setEnabled(false);
        etNewPassword.setEnabled(false);
        etConfirmNewPassword.setEnabled(false);
        etOldPassword.setEnabled(false);
        tilOldPassword.setVisibility(View.GONE);
        tilNewPassword.setVisibility(View.GONE);
        tilConfirmNewPassword.setVisibility(View.GONE);
        vLine.setVisibility(View.GONE);
        tvChangePassword.setVisibility(View.GONE);
        btnChangePassword.setVisibility(View.GONE);
        btnSaveProfile.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.VISIBLE);
    }
    private void logOut(){
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to Log Out?")
                .setPositiveButton("Log Out", (dialog, which) -> {

                    new AuthStorage(requireContext()).clearUser();
                    auth.signOut();

                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    requireActivity().finish();
                    startActivity(intent);


                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setAllergyCheckboxes(List<String> allergies) {

        cbMilk.setChecked(allergies.contains("Milk"));
        cbEggs.setChecked(allergies.contains("Eggs"));
        cbPeanuts.setChecked(allergies.contains("Peanuts"));
        cbTreeNuts.setChecked(allergies.contains("Tree Nuts"));
        cbSoy.setChecked(allergies.contains("Soybeans"));
        cbWheat.setChecked(allergies.contains("Wheat"));
        cbFish.setChecked(allergies.contains("Fish"));
        cbShellfish.setChecked(allergies.contains("Shellfish"));
        cbSesame.setChecked(allergies.contains("Sesame"));
        cbGluten.setChecked(allergies.contains("Gluten"));
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

    private void updateNamesOnPosts(String name, String uid){
        DatabaseReference rootRef = FirebaseDatabase
                .getInstance(DatabaseConstants.DATABASE_URL)
                .getReference();

        rootRef.child("users").child(uid).child("postList")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    if(!dataSnapshot.exists())return;

                    Map<String, Object> updateRef = new HashMap<>();

                    for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                        String postId = postSnapshot.getKey();
                        updateRef.put("posts/" + postId + "/username", name);
                        Log.d("Hello", "Im here");
                        Log.d("id", postId);
                    }

                    rootRef.updateChildren(updateRef)
                            .addOnSuccessListener(unused -> {
                                Log.d("UpdateName", "All posts updated successfully");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("UpdateName", "Failed to update posts", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateName", "Failed to fetch post list", e);
                });

        rootRef.keepSynced(true);
    }


}
