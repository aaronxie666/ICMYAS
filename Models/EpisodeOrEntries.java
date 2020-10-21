package icn.icmyas.Models;

import com.parse.ParseObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Author:  Bradley Wilson
 * Date: 21/07/2017
 * Package: icn.icmyas.Models
 * Project Name: ICMYAS
 */

public class EpisodeOrEntries implements Serializable {

    private String objectId, airDate, location, thumbnailUrl;
    private boolean isActive, isAired;
    transient private ArrayList<Model> modelsInEpisode;
    private int episodeNumber;
    transient private ParseObject transformedModel;
    private boolean isEntry;

    public ParseObject getTransformedModel() {
        return transformedModel;
    }

    public void setTransformedModel(ParseObject transformedModel) {
        this.transformedModel = transformedModel;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setAirDate(String airDate) {
        this.airDate = airDate;
    }

    public void setLocationOrName(String location) {
        this.location = location;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setIsAiredOrViewed(boolean aired) {
        isAired = aired;
    }

    public void setModelsInEpisode(ArrayList<Model> modelsInEpisode) {
        this.modelsInEpisode = modelsInEpisode;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getAirDate() {
        return airDate;
    }

    public String getLocationOrName() {
        return location;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isAiredOrViewed() {
        return isAired;
    }

    public ArrayList<Model> getModelsInEpisode() {
        return modelsInEpisode;
    }
    
    public boolean isEntry() {return isEntry;}
    
    public void setEntry(boolean isEntry) {
        this.isEntry = isEntry;
    }

    public EpisodeOrEntries() {

    }
}
