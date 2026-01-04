package myduckisgoingmad;

import addons.HavenUtil;
import haven.Config;
import haven.UI;
import haven.geoloc.Geoloc;
import haven.geoloc.GeolocException;
import jerklib.util.Pair;
import myduckisgoingmad.api.MapAPIClient;
import myduckisgoingmad.api.MapAPIException;

public class Tracker extends Thread {
    MapAPIClient client;
    boolean tracking = false;
    HavenUtil m_util;
    String lastTileHash = null;
    Pair<Double, Double> lastPlayerCoord = null;
    int trackCount = 0;

    private static class Holder {
        static final Tracker INSTANCE = new Tracker();
    }

    public static Tracker getInstance() {
        return Holder.INSTANCE;
    }

    private Tracker() {
        UI ui = UI.instance;
        m_util = ui.m_util;
        client = new MapAPIClient(Config.mapApiBaseUrl);

    }

    public void checkout() {
        try {
            Pair<Double, Double> coords = Geoloc.getPlayerCoords();
            client.checkout(m_util.myName(), coords.first, coords.second);
            m_util.sendMessage(String.format("[GPS] Check out position: %.2f, %.2f", coords.first, coords.second));
        } catch (MapAPIException e) {
            m_util.sendErrorMessage("Failed to checkout position");
        } catch (GeolocException e) {
            m_util.sendErrorMessage("Failed to get player coordinates");
        }
    }

    public void toggleTracking() {
        tracking = !tracking;
        trackCount = 0;
        lastTileHash = null;
        lastPlayerCoord = null;
        if (tracking) {
            m_util.sendMessage("[GPS] Tracking enabled");
        } else {
            m_util.sendMessage("[GPS] Tracking disabled");
        }
    }

    public void track() {
        if (!tracking) {
            return;
        }

        String currentTileHash = m_util.m_ui.slen.mini.getCurrentMapTileHash();

        if (lastTileHash == null || !lastTileHash.equals(currentTileHash)) {
            try {
                Pair<Double, Double> coords = Geoloc.getPlayerCoords();
                double dist = 0;
                if (lastPlayerCoord != null) {
                    dist = Math.sqrt(Math.pow(coords.first - lastPlayerCoord.first, 2)
                            + Math.pow(coords.second - lastPlayerCoord.second, 2));
                }

                if (dist > 0 && dist < 0.5) {
                    return;
                }

                m_util.sendMessage(String.format("Track position: %.2f, %.2f (moved %.4f units)", coords.first,
                        coords.second, dist));
                lastTileHash = currentTileHash;
                lastPlayerCoord = coords;
                client.checkout(m_util.myName(), coords.first, coords.second, true, trackCount == 0);
                ++trackCount;
            } catch (MapAPIException e) {
                m_util.sendErrorMessage("Failed to checkout position");
            } catch (GeolocException e) {
                // m_util.sendErrorMessage("Failed to get player coordinates");
            }
        }
    }

    public void prospect() {
        Prospector prospector = new Prospector(client, m_util);
        prospector.start();
    }

    public void dowse() {
        Dowser dowser = new Dowser(client, m_util);
        dowser.start();
    }
}
