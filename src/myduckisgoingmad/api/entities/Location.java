package myduckisgoingmad.api.entities;

public class Location {
    public String id;
    public double x;
    public double y;
    public String title;
    public String icon;
    public String color;
    public String notes;
    public Resource resource;
    public Claim claim;
    public PointOfInterest poi;

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
