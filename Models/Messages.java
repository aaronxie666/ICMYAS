package icn.icmyas.Models;

import java.io.Serializable;

/**
 * Author:  Bradley Wilson
 * Date: 21/08/2017
 * Package: icn.icmyas.Models
 * Project Name: ICMYAS
 */

public class Messages {

    private String senderName;
    private String message;
    private String objectId;

    public String getProfileURL() {
        return profileURL;
    }

    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    private String profileURL;
    private boolean isRead;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Messages() {

    }

    public String getObjectId(){
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
