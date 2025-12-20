package myduckisgoingmad;

import addons.HavenUtil;
import haven.Config;
import haven.UI;
import haven.geoloc.Geoloc;
import haven.geoloc.GeolocException;
import jerklib.util.Pair;
import myduckisgoingmad.api.MapAPIClient;
import myduckisgoingmad.api.MapAPIException;

public class Tracker {
    MapAPIClient client;
    boolean tracking = false;
    HavenUtil m_util;
    String username;

    private static class Holder {
        static final Tracker INSTANCE = new Tracker();
    }

    public static Tracker getInstance() {
        return Holder.INSTANCE;
    }

    private Tracker() {
        UI ui = UI.instance;
        m_util = ui.m_util;
        username = ui.root.ui.sess.charname;

        client = new MapAPIClient(Config.mapApiBaseUrl);

    }

    public void checkout() {
        try {
            Pair<Double, Double> coords = Geoloc.getPlayerCoords();
            client.checkout(username, coords.first, coords.second);
            m_util.sendMessage(String.format("Checked out position: %.2f, %.2f", coords.first, coords.second));
        } catch (MapAPIException e) {
            m_util.sendErrorMessage("Failed to checkout position");
            System.err.println("Failed to checkout position: " + e.getMessage());
        } catch (GeolocException e) {
            m_util.sendErrorMessage("Failed to get player coordinates");
            System.err.println("Failed to get player coordinates: " + e.getMessage());
        }
    }

    public void toggleTracking() {
        tracking = !tracking;
        if (tracking) {
            m_util.sendMessage("Tracking enabled");
        } else {
            m_util.sendMessage("Tracking disabled");
        }
    }

    public void prospect() {
        m_util.sendMessage("Prospecting nearby checkouts...");
    }
}
