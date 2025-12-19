package myduckisgoingmad.api.entities;

public class Claim {
    public ClaimType type;
    public ClaimStatus status;
    public String ownerName;
    public Double width;
    public Double height;

    public Claim(ClaimType type) {
        this.type = type;
    }
}
