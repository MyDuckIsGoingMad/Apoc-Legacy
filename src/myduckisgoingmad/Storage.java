package myduckisgoingmad;

import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

import haven.Gob;

public class Storage {
    private Map<Integer, Color> highlightItems;

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
}
