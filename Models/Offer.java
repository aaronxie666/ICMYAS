package icn.icmyas.Models;

import java.io.Serializable;

/**
 * Author:  Bradley Wilson
 * Date: 14/08/2017
 * Package: icn.icmyas.Models
 * Project Name: ICMYAS
 */

public class Offer {

    private String objectId;
    private String imageUrl;
    private String text;
    private String title;
    private String transparentImage;
    private String websiteURL;
    private String code;
    private int cost;
    private boolean isFeatured;
    private boolean isOffer;
    private boolean isGold;
    private int extra = 0;
    private int bonusStars = 0;

    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }

    public int getBonusStars() {
        return bonusStars;
    }

    public void setBonusStars(int bonusStars) {
        this.bonusStars = bonusStars;
    }

    public Offer() {

    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWebsiteURL() {
        return websiteURL;
    }

    public void setWebsiteURL(String websiteURL) {
        this.websiteURL = websiteURL;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getPrice() {
        return cost;
    }

    public void setPrice(int cost) {
        this.cost = cost;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public void setOffer(boolean offer) {
        this.isOffer = offer;
    }

    public boolean isOffer() {
        return isOffer;
    }

    public void setTransparentImage(String transparentImage) {
        this.transparentImage = transparentImage;
    }

    public String getTransparentImage() {
        return transparentImage;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isGold() {
        return isGold;
    }

    public void setGold(boolean gold) {
        isGold = gold;
    }
}
