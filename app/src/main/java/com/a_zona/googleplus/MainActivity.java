package com.a_zona.googleplus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.a_zona.googleplus.event.GoogleEventListener;
import com.a_zona.googleplus.fragment.GooglePlus;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

/**
 * Created by Alexey Filyushin (sqqrew@gmail.com) on 10.01.2015.
 * Package: com.a_zona.socialtemplates.fragments
 * Project: SocialTemplates
 * <p/>
 * Copyright (c) 2014 A-Zona, Inc.
 */

public class MainActivity extends FragmentActivity implements GoogleEventListener, View.OnClickListener{

    private GooglePlus fragment;
    private SignInButton btnSignIn;
    private Button btnSignOut;
    private Button btnRevoke;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GooglePlus.RC_SIGN_IN){
            GooglePlus fragment = (GooglePlus) getSupportFragmentManager().findFragmentByTag("google");
            fragment.onActivityResult(requestCode, resultCode, data);
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        if (savedInstanceState == null) {
            fragment = new GooglePlus();
            fragment.setListener(this);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, "google")
                    .commit();
        }

    }

    private void init(){
        btnSignIn = (SignInButton) findViewById(R.id.btnSignIn);
        btnSignOut = (Button) findViewById(R.id.btnSignOut);
        btnRevoke = (Button) findViewById(R.id.btnRevoke);
        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevoke.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return  item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Person currentPerson) {
        btnSignIn.setEnabled(false);
        btnSignOut.setEnabled(true);
        btnRevoke.setEnabled(true);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        btnSignIn.setEnabled(true);
        btnSignOut.setEnabled(false);
        btnRevoke.setEnabled(false);
    }

    @Override
    public void onResultData(PersonBuffer personBuffer) {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSignIn:
                fragment.signIn();
                break;
            case R.id.btnSignOut:
                fragment.signOut();
                break;
            case R.id.btnRevoke:
                fragment.revoke();
                break;
        }
    }
}
