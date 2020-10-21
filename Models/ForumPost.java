package icn.icmyas.Models;

import com.parse.ParseQuery;

import java.io.Serializable;
import java.util.ArrayList;

public class ForumPost implements Serializable {
    private String objectId;
    private String title;
    private String message;
    private String profilePictureUrl;
    private String userId;
    private String username;
    private String dateAndTime;
    private int totalReplies;
    private ArrayList<String> repliesArray;

    // is thread post
    public ForumPost(String objectId, String title, String message, String profilePictureUrl, String userId, String username, String timeAndDate, int totalReplies, ArrayList<String> repliesArray) {
        this.objectId = objectId;
        this.title = title;
        this.message = message;
        this.profilePictureUrl = profilePictureUrl;
        this.userId = userId;
        this.username = username;
        this.dateAndTime = timeAndDate;
        this.totalReplies = totalReplies;
        this.repliesArray = repliesArray;
    }

    // is thread reply
    public ForumPost(String objectId, String message, String profilePictureUrl, String userId, String username, String dateAndTime) {
        this.objectId = objectId;
        this.message = message;
        this.profilePictureUrl = profilePictureUrl;
        this.userId = userId;
        this.username = username;
        this.dateAndTime = dateAndTime;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public int getTotalReplies() {
        return totalReplies;
    }

    public ArrayList<String> getRepliesArray() {
        return repliesArray;
    }
}