package icn.icmyas.Models;

import com.parse.ParseObject;

import java.io.Serializable;

/**
 * Author:  Bradley Wilson
 * Date: 21/07/2017
 * Package: icn.icmyas.Models
 * Project Name: ICMYAS
 */

public class Model implements Serializable {

    private String objectId, name, beforeImage, afterImage;
    private ParseObject myStoryMyCause;
    private int pledgesTotal, transformationTotal;
    private boolean isBestTransformed;

    public boolean isBestTransformed() { return isBestTransformed; }

    public void setBestTransformed(boolean bestTransformed) { isBestTransformed = bestTransformed; }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBeforeImage(String beforeImage) {
        this.beforeImage = beforeImage;
    }

    public void setAfterImage(String afterImage) {
        this.afterImage = afterImage;
    }

    public void setMyStoryMyCause(ParseObject myStoryMyCause) {
        this.myStoryMyCause = myStoryMyCause;
    }

    public void setPledgesTotal(int pledgesTotal) {
        this.pledgesTotal = pledgesTotal;
    }

    public void setTransformationTotal(int transformationTotal) {
        this.transformationTotal = transformationTotal;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getName() {
        return name;
    }

    public String getBeforeImage() {
        return beforeImage;
    }

    public String getAfterImage() {
        return afterImage;
    }

    public ParseObject getMyStoryMyCause() {
        return myStoryMyCause;
    }

    public int getPledgesTotal() {
        return pledgesTotal;
    }

    public int getTransformationTotal() {
        return transformationTotal;
    }

    public Model() {

    }
}
