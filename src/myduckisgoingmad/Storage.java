package myduckisgoingmad;

import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

import haven.Coord;
import haven.Gob;
import haven.Label;
import haven.UI;
import haven.Widget;
import myduckisgoingmad.api.entities.Cattle;

public class Storage {
    private Map<Integer, Color> highlightItems;
    private int cattleInfoWndID;
    private Cattle currentCattle;

    public Storage() {
        highlightItems = new HashMap<Integer, Color>();
    }

    public void setHighlight(Gob g, Color color) {
        highlightItems.put(g.id, color);
    }

    public void clearHighlight(Gob g) {
        highlightItems.remove(g.id);
    }

    public boolean isHighlighted(Gob g) {
        return highlightItems.containsKey(g.id);
    }

    public Color getHighlight(Gob g) {
        return highlightItems.get(g.id);
    }

    public void setCattleInfoWnd(int id) {
        this.cattleInfoWndID = id;
        this.currentCattle = new Cattle();
    }

    public void clearCattleInfoWnd() {
        this.cattleInfoWndID = 0;
        this.currentCattle = null;
    }

    public boolean isCattleInfoWnd(int id) {
        return cattleInfoWndID != 0 && cattleInfoWndID == id;
    }

    public void processCattleInfo(String type, Object[] args) {
        if (currentCattle == null) {
            return;
        }

        boolean complete = currentCattle.processInfo(type, args);
        if (complete) {
            enrichCattleInfo();
        }
    }

    public Cattle currentCattle(int id) {
        return currentCattle;
    }

    protected void enrichCattleInfo() {
        if (currentCattle == null || cattleInfoWndID == 0) {
            return;
        }

        Widget widget = UI.instance.widgets.get(cattleInfoWndID);

        int posX = 170;
        int posY = 105;
        int valueDx = 65;

        new Label(new Coord(posX, posY), widget, "Milk score:");
        new Label(new Coord(posX + valueDx, posY), widget, String.valueOf(Math.round(currentCattle.getMilkScore())));
        posY += 15;

        new Label(new Coord(posX, posY), widget, "Meat score:");
        new Label(new Coord(posX + valueDx, posY), widget, String.valueOf(Math.round(currentCattle.getMeatScore())));
        posY += 15;

        new Label(new Coord(posX, posY), widget, "Prod score:");
        new Label(new Coord(posX + valueDx, posY), widget,
                String.valueOf(Math.round(currentCattle.getProductionScore())));
        posY += 15;

        new Label(new Coord(posX, posY), widget, "Total score:");
        new Label(new Coord(posX + valueDx, posY), widget, String.valueOf(Math.round(currentCattle.getTotalScore())));
    }
}
