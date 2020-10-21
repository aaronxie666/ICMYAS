package icn.icmyas.Models;

public class Judge {

    String name, votedFor, description, image;

    public Judge(String name, String votedFor, String description, String image) {
        this.name = name;
        this.votedFor = votedFor;
        this.description = description;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getVotedFor() {
        return votedFor;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }
}
