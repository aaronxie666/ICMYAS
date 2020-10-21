package icn.icmyas.Models;

public class LeaderboardModel {

    private String name;
    private int total;

    public LeaderboardModel(String name, int total) {
        this.name = name;
        this.total = total;
}

    public String getName() {
        return name;
    }

    public int getTotal() {
        return total;
    }
}