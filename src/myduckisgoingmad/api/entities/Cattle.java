package myduckisgoingmad.api.entities;

import haven.Coord;
import haven.Label;
import haven.Widget;

public class Cattle {
    private int id;
    private String name;
    private Gender gender;
    private int meatQuality;
    private int milkQuality;
    private int hideQuality;
    private int meatQuantity;
    private int milkQuantity;
    private int breedingQuantity;

    private int cattleInfoWndLabelCount;
    private boolean complete;

    public Cattle() {
        this.cattleInfoWndLabelCount = 0;
        this.complete = false;
    }

    public Cattle(int id, String name, Gender gender, int meatQuality, int milkQuality, int hideQuality,
            int meatQuantity, int milkQuantity, int breedingQuantity) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.meatQuality = meatQuality;
        this.milkQuality = milkQuality;
        this.hideQuality = hideQuality;
        this.meatQuantity = meatQuantity;
        this.milkQuantity = milkQuantity;
        this.breedingQuantity = breedingQuantity;
        this.complete = true;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = Gender.fromString(gender);
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getMeatQuality() {
        return meatQuality;
    }

    public void setMeatQuality(int meatQuality) {
        this.meatQuality = meatQuality;
    }

    public int getMilkQuality() {
        return milkQuality;
    }

    public void setMilkQuality(int milkQuality) {
        this.milkQuality = milkQuality;
    }

    public int getHideQuality() {
        return hideQuality;
    }

    public void setHideQuality(int hideQuality) {
        this.hideQuality = hideQuality;
    }

    public int getMeatQuantity() {
        return meatQuantity;
    }

    public void setMeatQuantity(int meatQuantity) {
        this.meatQuantity = meatQuantity;
    }

    public int getMilkQuantity() {
        return milkQuantity;
    }

    public void setMilkQuantity(int milkQuantity) {
        this.milkQuantity = milkQuantity;
    }

    public int getBreedingQuantity() {
        return breedingQuantity;
    }

    public void setBreedingQuantity(int breedingQuantity) {
        this.breedingQuantity = breedingQuantity;
    }

    public String toString() {
        return String.format(
                "Cattle{id=%d, name='%s', gender=%s, meatQuality=%d, milkQuality=%d, hideQuality=%d, meatQuantity=%d, milkQuantity=%d, breedingQuantity=%d}",
                id, name, gender, meatQuality, milkQuality, hideQuality, meatQuantity, milkQuantity, breedingQuantity);
    }

    // Return true if cattle info is complete after processing this info, false
    // otherwise
    public boolean processInfo(String type, Object[] args) {
        if (complete) {
            return false;
        }

        if (type.equals("av")) {
            if (args.length >= 1 && args[0] instanceof Number) {
                Number cattleID = (Number) args[0];
                setID(cattleID.intValue());
            }
        } else if (type.equals("text") && args.length == 2) {
            setName(args[1].toString());
        } else if (type.equals("lbl")) {
            switch (cattleInfoWndLabelCount) {
            case 0:
                setGender(args[0].toString());
                break;
            case 2:
                setMeatQuality(Integer.parseInt(args[0].toString()));
                break;
            case 4:
                setMilkQuality(Integer.parseInt(args[0].toString()));
                break;
            case 6:
                setHideQuality(Integer.parseInt(args[0].toString()));
                break;
            case 8:
                setMeatQuantity(Integer.parseInt(args[0].toString()));
                break;
            case 10:
                setMilkQuantity(Integer.parseInt(args[0].toString()));
                break;
            case 12:
                setBreedingQuantity(Integer.parseInt(args[0].toString()));
                this.complete = true;
                break;
            default:
                break;
            }
            ++cattleInfoWndLabelCount;
        }
        return cattleInfoWndLabelCount == 13;
    }

    public double getMeatScore() {
        if (!complete) {
            return 0;
        }

        return Math.sqrt((double) meatQuality * meatQuantity);
    }

    public double getMilkScore() {
        if (!complete) {
            return 0;
        }

        return Math.sqrt((double) milkQuality * milkQuantity);
    }

    public double getProductionScore() {
        return 0.4 * getMeatScore() + 0.6 * getMilkScore();
    }

    public double getTotalScore() {
        if (!complete) {
            return 0;
        }

        if (gender == Gender.MALE) {
            return 0.3 * getProductionScore() + 0.7 * breedingQuantity;
        } else {
            return 0.6 * getProductionScore() + 0.4 * breedingQuantity;
        }
    }

    public void enrichCattleInfo(Widget widget) {
        if (!complete || widget == null) {
            return;
        }

        int posX = 170;
        int posY = 105;
        int valueDx = 65;

        new Label(new Coord(posX, posY), widget, "Milk score:");
        new Label(new Coord(posX + valueDx, posY), widget, String.valueOf(Math.round(getMilkScore())));
        posY += 15;

        new Label(new Coord(posX, posY), widget, "Meat score:");
        new Label(new Coord(posX + valueDx, posY), widget, String.valueOf(Math.round(getMeatScore())));
        posY += 15;

        new Label(new Coord(posX, posY), widget, "Prod score:");
        new Label(new Coord(posX + valueDx, posY), widget, String.valueOf(Math.round(getProductionScore())));
        posY += 15;

        new Label(new Coord(posX, posY), widget, "Total score:");
        new Label(new Coord(posX + valueDx, posY), widget, String.valueOf(Math.round(getTotalScore())));
    }

}
