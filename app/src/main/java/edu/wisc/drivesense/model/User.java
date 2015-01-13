package edu.wisc.drivesense.model;

import com.google.gson.annotations.Expose;
import com.orm.SugarRecord;

/**
 * Created by Damouse on 8/5/14.
 */
public class User extends SugarRecord<User> {
    @Expose
    public int userId;

    @Expose
    public String email;

    @Expose
    public String authenticationToken;

    public int group_id;
    public Boolean admin;
    public boolean loggedIn;

    //preferences
    private boolean automaticRecording;
    private boolean automaticUnpoweredRecording;
    private boolean automaticUploading;
    private boolean automaticUploadOffWifi;
    private boolean automaticDelete;

    public User() {
        userId = -1;
        email = "";
        authenticationToken = "";
        group_id = -1;
        admin = false;

        automaticRecording = false;
        automaticUnpoweredRecording = false;
        automaticUploading = false;
        automaticUploadOffWifi = false;
        automaticDelete = false;
    }


    /* Public Interface */
    /** The demo user isnt really "demo," its the default state of the app. Any trips for the demo user
     * can be created without forcing a login. This user has no permissions and cannot upload.
     */
    public boolean demoUser() {
        return userId == -7;
    }


    /* Boilerplate Java Overrides */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("id: " + userId);
        sb.append(" email: " + email);
        sb.append(" auth_token: " + authenticationToken);
        sb.append(" group_id: " + group_id);
        sb.append(" admin: " + admin);

        return sb.toString();
    }

    public void setLoggedIn(String authToken) {
        authenticationToken = authToken;
        loggedIn = true;
        this.save();
    }

    public void logOut() {
        authenticationToken = "";
        loggedIn = false;
        this.save();
    }


    /* Preference Accessors */
    public boolean isAutomaticRecording() {
        return automaticRecording;
    }

    public void setAutomaticRecording(boolean automaticRecording) {
        if (!demoUser())
            this.automaticRecording = automaticRecording;
    }

    public boolean isAutomaticUnpoweredRecording() {
        return automaticUnpoweredRecording;
    }

    public void setAutomaticUnpoweredRecording(boolean automaticUnpoweredRecording) {
        if (!demoUser())
            this.automaticUnpoweredRecording = automaticUnpoweredRecording;
    }

    public boolean isAutomaticUploading() {
        return automaticUploading;
    }

    public void setAutomaticUploading(boolean automaticUploading) {
        if (!demoUser())
            this.automaticUploading = automaticUploading;
    }

    public boolean isAutomaticUploadOffWifi() {
        return automaticUploadOffWifi;
    }

    public void setAutomaticUploadOffWifi(boolean automaticUploadOffWifi) {
        if (!demoUser())
            this.automaticUploadOffWifi = automaticUploadOffWifi;
    }

    public boolean isAutomaticDelete() {
        return automaticDelete;
    }

    public void setAutomaticDelete(boolean automaticDelete) {
        if (!demoUser())
            this.automaticDelete = automaticDelete;
    }
}
