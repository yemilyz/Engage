package com.codepath.engage.models;

import com.google.firebase.database.IgnoreExtraProperties;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by awestort on 7/18/17.
 */

@IgnoreExtraProperties
@Parcel
public class User {
    public String uid;

    public String firstName;
    public String lastName;
    public String email;
    public String profilePicture;
    public int numFollowers;
    public int numFollowing;
    public HashMap<String,String> followers;
    public HashMap<String,String>  following;
    public HashMap<String,String> notifList;
    public ArrayList<String> notifImg;
    public List<String> eventsList;
    public ArrayList<String> notList;
    public ArrayList<String> imgList;
    public String firebaseToken;

    public User(){
    }

    public User(String uid, String firstName, String lastName, String email, String profilePicture, int numFollowers, int numFollowing, HashMap<String,String> followers, HashMap<String,String> following, String firebaseToken, HashMap<String,String> notList, ArrayList<String> imgs) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.profilePicture = profilePicture;
        this.numFollowers = numFollowers;
        this.numFollowing = numFollowing;
        this.following = following;
        this.followers = followers;
        this.notifList = notList;
        this.firebaseToken = firebaseToken;
        this.notifImg = imgs;

    }

    public ArrayList<String> getNotifImg() {
        return notifImg;
    }

    public void setNotifImg(ArrayList<String> notifImg) {
        this.notifImg = notifImg;
    }

    public ArrayList<String> getNotList() {
        return notList;
    }

    public void setNotList(ArrayList<String> notList) {
        this.notList = notList;
    }

    public ArrayList<String> getImgList() {
        return imgList;
    }

    public void setImgList(ArrayList<String> imgList) {
        this.imgList = imgList;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public HashMap<String, String> getNotifList() {
        return notifList;
    }

    public void setNotifList(HashMap<String, String> notifList) {
        this.notifList = notifList;
    }

//
//    public ArrayList<String> getNotifList() {
//        return notifList;
//    }
//
//    public void setNotifList(ArrayList<String> notifList) {
//        this.notifList = notifList;
//    }

    public List<String> getEventsList() {
        return eventsList;
    }

    public void setEventsList(List<String> eventsList) {
        this.eventsList = eventsList;
    }

    public HashMap<String,String> getFollowers() {
        return followers;
    }

    public void setFollowers(HashMap<String,String> followers) {
        this.followers = followers;
    }

    public HashMap<String,String> getFollowing() {
        return following;
    }

    public void setFollowing(HashMap<String,String> following) {
        this.following = following;
    }

    public String getUid() { return uid; }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() { return lastName; }

    public String getEmail() {
        return email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public int getNumFollowers() { return numFollowers; }

    public void setNumFollowers(int numFollowers) { this.numFollowers = numFollowers; }

    public int getNumFollowing() { return numFollowing; }

    public void setNumFollowing(int numFollowing) { this.numFollowing = numFollowing; }
}