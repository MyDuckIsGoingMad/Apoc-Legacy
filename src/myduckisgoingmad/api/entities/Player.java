package myduckisgoingmad.api.entities;

public class Player {
    public String id;
    public String username;
    public double lastX;
    public double lastY;

    public Player(String username, double lastX, double lastY) {
        this.username = username;
        this.lastX = lastX;
        this.lastY = lastY;
    }
}
