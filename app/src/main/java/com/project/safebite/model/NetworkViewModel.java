package com.project.safebite.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class NetworkViewModel extends AndroidViewModel {

    private MutableLiveData<Boolean> isConnected = new MutableLiveData<>();

    public NetworkViewModel(@NonNull Application application) {
        super(application);
    }

    public void setConnected(boolean connected) {
        isConnected.postValue(connected);
    }

    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }
}
