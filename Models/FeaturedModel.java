package icn.icmyas.Models;

/**
 * Author:  Bradley Wilson
 * Date: 17/07/2017
 * Package: icn.icmyas.Models
 * Project Name: ICMYAS
 */

public class FeaturedModel {

    public FeaturedModel() {
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public int getUserVotes() {
        return userVotes;
    }

    private String profilePictureUrl;
    private int userVotes;

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void setUserVotes(int userVotes) {
        this.userVotes = userVotes;
    }
}
