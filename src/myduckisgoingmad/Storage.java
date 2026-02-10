package myduckisgoingmad;

import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

import haven.Gob;
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
            Widget widget = UI.instance.widgets.get(cattleInfoWndID);
            currentCattle.enrichCattleInfo(widget);
        }
    }

    public Cattle currentCattle(int id) {
        return currentCattle;
    }
}
