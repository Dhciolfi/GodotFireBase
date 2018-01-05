/**
 * Copyright 2017 FrogSquare. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package org.godotengine.godot.auth;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.godot.game.R;

import org.json.JSONObject;
import org.json.JSONException;

import org.godotengine.godot.Utils;

public class EmailPasswordSignIn {

	public static EmailPasswordSignIn getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new EmailPasswordSignIn (p_activity);
		}

		return mInstance;
	}

	public EmailPasswordSignIn (Activity p_activity) {
		activity = p_activity;
	}

	public void init() {
		mAuth = FirebaseAuth.getInstance();

		mAuthListener = new FirebaseAuth.AuthStateListener() {

			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if (user != null) {
					Utils.d("E&P:onAuthStateChanged:signed_in:" + user.getUid());
					successSignIn(user);
				} else {
					// User is signed out
					Utils.d("E&P:onAuthStateChanged:signed_out");
					successSignOut();
				}
			}
		};
        
        onStart();
	}

	public void createAccount(final String email, final String password) {
		Utils.d("E&P:CreateAccount:" + email);

		mAuth.createUserWithEmailAndPassword(email, password)
		.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				Utils.d("E&P:CreateUserWithEmail:onComplete:" + task.isSuccessful());

				if (!task.isSuccessful()) {
					Utils.d("E&P:CreateAccount:Error:"  + task.getException());
                    Utils.callScriptFunc("E&P", "CreateAccount", false);
				} else {
                    Utils.d("E&P:CreateAccount:Complete");
                    Utils.callScriptFunc("E&P", "CreateAccount", true);
                }
			}
		});
	}

	public void signIn(final String email, final String password) {
        Utils.d("E&P:SignIn:" + email);
        
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Utils.d("E&P:SignIn:Sucess");
                    FirebaseUser user = mAuth.getCurrentUser();
                    successSignIn(user);
                } else {
                    Utils.d("E&P:SignIn:Error:" + task.getException());
                    Utils.callScriptFunc("E&P", "SignIn", false);
                }
            }
        });
	}

	public void signOut() {
        mAuth.signOut();
        
        successSignOut();
	}

	private void successSignIn(FirebaseUser user) {
		Utils.d("E&P:SignIn:Success");
        
        isEmailConnected = true;
        
        try {
			currentEmailUser.put("email", user.getEmail());
			currentEmailUser.put("uid", user.getUid());
		} catch (JSONException e) { Utils.d("Email:JSON:Error:" + e.toString()); }

		Utils.callScriptFunc("Auth", "EmailLogin", true);
	}
    
    private void successSignOut() {
        Utils.d("E&P:SignOut:Success");
        
        isEmailConnected = false;
        
        Utils.callScriptFunc("Auth", "EmailLogin", false);
    }
    
    public boolean isConnected() {
        return isEmailConnected;
    }
    
    public String getUserDetails() {
        return currentEmailUser.toString();
    }

	public void onStart() {
        FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) { successSignIn(user); }
        
		mAuth.addAuthStateListener(mAuthListener);
	}

	public void onStop() {
		if (mAuthListener != null) { mAuth.removeAuthStateListener(mAuthListener); }
	}

	private static Activity activity = null;
	private static EmailPasswordSignIn mInstance = null;
    
    private static boolean isEmailConnected = false;
    private JSONObject currentEmailUser = new JSONObject();

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;
}
