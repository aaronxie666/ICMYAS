package icn.icmyas.Models;

/**
 * Author:  Bradley Wilson
 * Date: 17/07/2017
 * Package: icn.icmyas.Models
 * Project Name: ICMYAS
 */

public class LatestVideos {

    // Getter and Setter model for recycler view items
    private String title;
    private String image;
    private String url;

    public LatestVideos(String title, String image, String url) {

        this.title = title;
        this.image = image;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getUrl() {return url;}
}
