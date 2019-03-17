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

/**
 * Modified by Daniel Ciolfi <daniel.ciolfi@gmail.com>
 **/

package org.godotengine.godot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONException;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import com.google.android.gms.tasks.*;

import org.godotengine.godot.Dictionary;

public class Firestore {

	public static Firestore getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new Firestore(p_activity);
		}

		return mInstance;
	}

	public Firestore(Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;

		// Enable Firestore logging
		FirebaseFirestore.setLoggingEnabled(true);
		db = FirebaseFirestore.getInstance();
        
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build();
        
        db.setFirestoreSettings(settings);
    
        listeners = new HashMap<String, ListenerRegistration>();

		Utils.d("GodotFireBase", "Firestore::Initialized");
	}

	public void loadDocuments (final String p_name, final int callback_id) {
		Utils.d("GodotFireBase", "Firestore::LoadData");

		db.collection(p_name).get()
		.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
			@Override
			public void onComplete(@NonNull Task<QuerySnapshot> task) {
				if (task.isSuccessful()) {
					JSONObject jobject = new JSONObject();

					try {
	    				JSONObject jobject_1 = new JSONObject();

						for (DocumentSnapshot document : task.getResult()) {
							jobject_1.put(document.getId(), new JSONObject(document.getData()));
						}

                        jobject.put(p_name, jobject_1);
					} catch (JSONException e) {
						Utils.d("GodotFireBase", "JSON Exception: " + e.toString());
					}

                    
			    	Utils.d("GodotFireBase", "Data: " + jobject.toString());

                    if (callback_id == -1) {
    					Utils.callScriptFunc(
                                "Firestore", "Documents", jobject.toString());
                    } else {
  						Utils.callScriptFunc(
                                callback_id, "Firestore", "Documents", jobject.toString());
                    }

				} else {
					Utils.w("GodotFireBase", "Error getting documents: " + task.getException());
				}
			}
		});
	}

	public void addDocument (final String p_name, final Dictionary p_dict) {
		Utils.d("GodotFireBase", "Firestore::AddData");

		// Add a new document with a generated ID
		db.collection(p_name)
		.add(p_dict) // AutoGrenerate ID use .document("name").set(p_dict)
		.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
			@Override
			public void onSuccess(DocumentReference documentReference) {
				Utils.d("GodotFireBase", "DocumentSnapshot added with ID: " + documentReference.getId());
				Utils.callScriptFunc("Firestore", "DocumentAdded", true);
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Utils.w("GodotFireBase", "Error adding document: " + e);
				Utils.callScriptFunc("Firestore", "DocumentAdded", false);
			}
		});
	}

	public void setData(final String p_col_name, final String p_doc_name, final Dictionary p_dict) {
		db.collection(p_col_name).document(p_doc_name)
		.set(p_dict, SetOptions.merge())
		.addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Utils.d("GodotFireBase", "DocumentSnapshot successfully written!");
				Utils.callScriptFunc("Firestore", "DocumentAdded", true);
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Utils.w("GodotFireBase", "Error adding document: " + e);
				Utils.callScriptFunc("Firestore", "DocumentAdded", false);
			}
		});

	}
    
    public void setListener(final String p_col_name, final String p_doc_name){
        if (listeners.containsKey(p_col_name + "/" + p_doc_name)) {
            Utils.d("GodotFireBase", "Listener já existente!");
            return;
        }
        listeners.put(p_col_name + "/" + p_doc_name,db.collection(p_col_name).document(p_doc_name).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Utils.w("GodotFireBase", "Error seting listener: " + e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Utils.d("GodotFireBase", "Current data: " + snapshot.getData());
                    Utils.callScriptFunc("Firestore", "SnapshotData", new JSONObject(snapshot.getData()).toString());
                } else {
                    Utils.d("GodotFireBase", "Current data: null");
                    Utils.callScriptFunc("Firestore", "SnapshotData", "");
                }
            }
        }));
    }
    
    public void removeListener(final String p_col_name, final String p_doc_name){
        if (listeners.containsKey(p_col_name + "/" + p_doc_name)){
            listeners.get(p_col_name + "/" + p_doc_name).remove();
            listeners.remove(p_col_name + "/" + p_doc_name);
            Utils.d("GodotFireBase", "Listener removido!");
        }
    }

	private FirebaseFirestore db = null;
	private static Activity activity = null;
	private static Firestore mInstance = null;

    private int script_callback_id = -1;
    
    private static HashMap<String, ListenerRegistration> listeners = null;

	private FirebaseApp mFirebaseApp = null;
}
