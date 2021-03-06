package com.codepath.engage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codepath.engage.models.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private String TAG = "LOGIN_ACTIVITY";

    public static CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ValueEventListener valueEventListener;
    private DatabaseReference mDatabase;
    private AccessToken accessToken;
    private FirebaseAuth.AuthStateListener authListener;
    private Boolean authFlag;

    LoginButton loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        mCallbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Write a message to the database
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        accessToken = AccessToken.getCurrentAccessToken();

        authFlag = false;

        loginButton = (LoginButton) findViewById(R.id.login_button);

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                if (!authFlag) {
                    if (user != null) {
                        if (accessToken != null) {
                            if (mDatabase == null) {
                                loginButton.setVisibility(View.VISIBLE);
                                //check if user in database
                            } else {
                                authFlag = true;
                                mDatabase.addListenerForSingleValueEvent(valueEventListener);
                            }
                        } else {
                            // No user is signed in
                            loginButton.setVisibility(View.VISIBLE);
                            Log.d(TAG, "User is not signed in or is null");
                        }

                    } else {
                        loginButton.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this, "Please sign in", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean yes = false;
                for (DataSnapshot userData : dataSnapshot.getChildren()) {
                    if (userData.getKey().equals(Profile.getCurrentProfile().getId())) {
                        Intent in = new Intent(LoginActivity.this, HomePage.class);
                        yes = true;
                        startActivity(in);
                        mAuth.removeAuthStateListener(authListener);
                        finish();
                    }
                }
                if (!yes){
                    mAuth.signOut();
                    LoginManager.getInstance().logOut();
                    loginButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);

                handleFacebookAccessToken(loginResult.getAccessToken());
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback(){
                    @Override
                    public void onCompleted(final JSONObject object, GraphResponse response) {
                        mDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean isInside = false;
                                for (DataSnapshot evSnapshot : dataSnapshot.getChildren()) {
                                    String k = evSnapshot.getKey();
                                    try {
                                        if (k.equals(object.getString("id"))) {
                                            isInside = true;
                                            Intent intent = new Intent(LoginActivity.this, HomePage.class);
                                            startActivity(intent);
                                            overridePendingTransition(0, 0);
                                            finish();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if(!isInside) {
                                    final User user = new User();
                                    Bundle bFacebookData = getFacebookData(object);
                                    Log.d(TAG, "facebook:onCompleted");
                                    user.setNumFollowers(0);
                                    user.setFollowers(new HashMap<String, String>());
                                    user.setFollowing(new HashMap<String, String>());
                                    user.setNumFollowing(0);
                                    try {
                                        String id = object.getString("id");
                                        user.setUid(id);
                                        Log.d(TAG, "facebook id");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        String first_name = object.getString("first_name");
                                        user.setFirstName(first_name);
                                        Log.d(TAG, "facebook first_name");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        String last_name = object.getString("last_name");
                                        user.setLastName(last_name);
                                        Log.d(TAG, "facebook last_name");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        String email = object.getString("email");
                                        user.setEmail(email);
                                        Log.d(TAG, "facebook email");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        URL profile_picture = new URL("https://graph.facebook.com/" + user.getUid() + "/picture?width=200&height=200");
                                        String profilePicture = profile_picture.toString();
                                        user.setProfilePicture(profilePicture);
                                        Log.d(TAG, "facebook profilePicture");
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                        writeNewUser(user.getUid(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getProfilePicture(), 0, 0, new HashMap<String, String>(), new HashMap<String, String>(), bFacebookData);
                                }

                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error. Check your internet connection.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "facebook:onError", error);

            }

            private Bundle getFacebookData(JSONObject object) {

                try {
                    Bundle bundle = new Bundle();
                    String id = object.getString("id");

                    try {
                        URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=200");
                        Log.i("profile_pic", profile_pic + "");
                        bundle.putString("profile_pic", profile_pic.toString());

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        return null;
                    }
                    bundle.putString("idFacebook", id);
                    if (object.has("first_name"))
                        bundle.putString("first_name", object.getString("first_name"));
                    if (object.has("last_name"))
                        bundle.putString("last_name", object.getString("last_name"));
                    if (object.has("email"))
                        bundle.putString("email", object.getString("email"));

                    return bundle;
                }
                catch(JSONException e) {
                    Toast.makeText(LoginActivity.this, "Failed to parse properly", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"Error parsing JSON");
                }
                return null;
            }
        });



    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        loginButton.setVisibility(View.GONE);
        mAuth.addAuthStateListener(authListener);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    public void writeNewUser(final String uid, String firstName, String lastName, String email, String profilePicture, int numFollowers, int numFollowing, HashMap<String, String> followers, HashMap<String, String> following, final Bundle facebookData) {

//, List<String> eventsList

        final User user = new User(uid, firstName, lastName, email, profilePicture, numFollowers, numFollowing, followers, following,"", null, null);
        mDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid)) {
                    Intent intent = new Intent (LoginActivity.this, HomePage.class);
                    intent.putExtras(facebookData);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                } else {
                    mDatabase.child(uid).setValue(user);
                    Intent intent = new Intent(LoginActivity.this, HomePage.class);
                    intent.putExtras(facebookData);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}