package com.a_zona.googleplus.fragment;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;


import com.a_zona.googleplus.event.GoogleEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

/**
 * Created by Alexey Filyushin (sqqrew@gmail.com) on 10.01.2015.
 * Package: com.a_zona.socialtemplates.fragments
 * Project: SocialTemplates
 * <p/>
 * Copyright (c) 2014 A-Zona, Inc.
 */

public final class GooglePlus extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<People.LoadPeopleResult> {

    private static final String TAG = GooglePlus.class.getSimpleName();

    private static final int STATE_DEFAULT = 0;

    private static final int STATE_SIGN_IN = 1;

    private static final int STATE_IN_PROGRESS = 2;

    private static final int STATE_SIGN_IN_SUCCESS = 3;

    public static final int RC_SIGN_IN = 0;


    private static final String SAVED_PROGRESS = "sign_in_progress";

    private GoogleApiClient mGoogleApiClient;

    private int mSignInProgress;

    private PendingIntent mSignInIntent;

    private Person mPerson = null;

    private PersonBuffer mPersonBuffer;

    private GoogleEventListener mGoogleEventListener;

    public GooglePlus() {
    }

    public void setListener(GoogleEventListener listener){
        this.mGoogleEventListener = listener;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == getActivity().RESULT_OK) {
                    mSignInProgress = STATE_SIGN_IN;
                } else {
                    mSignInProgress = STATE_DEFAULT;
                }

                if (!mGoogleApiClient.isConnecting()) {
                    connect();
                }
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = buildGoogleApiClient();
        if (mGoogleApiClient == null){
            Log.d(TAG, "Object GoogleApiClient == null");
        }

        if (savedInstanceState != null){
            mSignInProgress = savedInstanceState.getInt(SAVED_PROGRESS, STATE_DEFAULT);
        }

    }

/*    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_google, container, false);

        mGoogleApiClient = buildGoogleApiClient();
        if (mGoogleApiClient == null){
            Log.d(TAG, "Object GoogleApiClient == null");
        }

        if (savedInstanceState != null){
            mSignInProgress = savedInstanceState.getInt(SAVED_PROGRESS, STATE_DEFAULT);
        }
        return rootView;
    }*/

    @Override
    public void onStart() {
        super.onStart();
        connect();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_PROGRESS, mSignInProgress);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            mPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        }

        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Log.d(TAG, email);

        mSignInProgress = STATE_SIGN_IN_SUCCESS;

        if (mGoogleEventListener != null){
            mGoogleEventListener.onConnected(mPerson);
        }else {
            Log.d(TAG, "GoogleEventListener not specified");
        }

        Log.d(TAG, "Connect request has successfully completed.");
    }


    @Override
    public void onConnectionSuspended(int i) {
        switch (i){
            case CAUSE_NETWORK_LOST:
                Log.d(TAG, "Device connection was lost.");
                break;
            case CAUSE_SERVICE_DISCONNECTED:
                Log.d(TAG, "Service has been killed.");
                break;
        }
        connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            Log.d(TAG, "One of the API components you attempted to connect to is not available.");
        } else if (mSignInProgress != STATE_IN_PROGRESS) {
            mSignInIntent = result.getResolution();
            if (mSignInProgress == STATE_SIGN_IN) {
                resolveSignInError();
            }
            Log.d(TAG, "Connection failed: error code = " + result.getErrorCode());
        }

        if (mGoogleEventListener != null){
            mGoogleEventListener.onConnectionFailed(result);
        }else {
            Log.d(TAG, "GoogleEventListener not specified");
        }
    }

    @Override
    public void onResult(People.LoadPeopleResult peopleData) {
        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            mPersonBuffer = peopleData.getPersonBuffer();
            if (mGoogleEventListener != null){
                mGoogleEventListener.onResultData(peopleData.getPersonBuffer());
            }else {
                Log.d(TAG, "GoogleEventListener not specified");
            }

        } else {
            Log.e(TAG, "Error requesting visible circles: " + peopleData.getStatus());
        }
    }

    private GoogleApiClient buildGoogleApiClient() {
        Log.d(TAG, "Builds a new GoogleApiClient object for communicating with the Google APIs.");
        return new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    private void connect(){
        mGoogleApiClient.connect();
        Log.d(TAG, "Connects the client to Google Play services.");
    }

    private void disconnect(){
        mGoogleApiClient.disconnect();
        Log.d(TAG, "Closes the connection to Google Play services.");
    }

    private void resolveSignInError() {
        if (mSignInIntent != null) {
            try {
                mSignInProgress = STATE_IN_PROGRESS;
                getActivity().startIntentSenderForResult(mSignInIntent.getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.i(TAG, "Sign in intent could not be sent: "
                        + e.getLocalizedMessage());
                mSignInProgress = STATE_SIGN_IN;
                connect();
            }
        }
    }

    public void signIn(){
        resolveSignInError();
    }

    public void signOut(){
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        disconnect();
        connect();
    }

    public void revoke(){
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
        mGoogleApiClient = buildGoogleApiClient();
        connect();
    }

    public Person getPerson(){
        return mPerson;
    }

    public PersonBuffer getPersonBuffer(){
        return mPersonBuffer;
    }
}
