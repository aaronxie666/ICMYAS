package icn.icmyas.Models;

/**
 * Author:  Bradley Wilson
 * Date: 18/07/2017
 * Package: icn.icmyas.Models
 * Project Name: ICMYAS
 */

public class GalleryImage {

    private String imageURL;
    private boolean isImage, isCamera;

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public String getImageURL() {
        return imageURL;
    }

    public boolean isImage() {
        return isImage;
    }

    public void GalleryImage() {

    }

    public void setCamera(boolean camera) {
        this.isCamera = camera;
    }

    public boolean isCamera() {
        return isCamera;
    }
}
