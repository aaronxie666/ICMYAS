package icn.icmyas.Models;

import java.io.Serializable;

public class Agency implements Serializable {

    private String name, bio, country, logoUrl, bannerUrl, url;

    public Agency(String name, String bio, String country, String logoUrl, String bannerUrl, String url) {
        this.name = name;
        this.bio = bio;
        this.country = country;
        this.logoUrl = logoUrl;
        this.bannerUrl = bannerUrl;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getBio() {
        return bio;
    }

    public String getCountry() {
        return country;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public String getUrl() {
        return url;
    }

}
