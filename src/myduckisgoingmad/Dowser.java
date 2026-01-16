package myduckisgoingmad;

import java.util.ArrayList;

import addons.HavenUtil;
import haven.Coord;
import haven.Inventory;
import haven.Item;
import haven.geoloc.Geoloc;
import haven.geoloc.GeolocException;
import jerklib.util.Pair;
import myduckisgoingmad.api.MapAPIClient;
import myduckisgoingmad.api.MapAPIException;

public class Dowser extends Thread {
    HavenUtil m_util;
    MapAPIClient client;

    public Dowser(MapAPIClient client, HavenUtil util) {
        this.client = client;
        this.m_util = util;
    }

    public void run() {
        m_util.stop = false;
        m_util.update();
        m_util.sendMessage("[GPS] Dowsing ...");

        m_util.openInventory();
        Inventory bag = m_util.getInventory("Inventory");

        if (bag == null) {
            m_util.sendErrorMessage("[GPS] Cannot find player inventory");
            return;
        }

        Item flask = findFlaskOrBucket();

        if (!flask.tooltip.contains("Empty")) {
            m_util.itemAction(flask);
            m_util.autoFlowerMenu("Empty");
            m_util.wait(200);
        }

        Coord playerCoord = m_util.getPlayerCoord();
        Coord flaskBagCoord = flask.c;
        m_util.pickUpItem(flask);
        m_util.wait(200);
        m_util.itemAction(playerCoord);
        m_util.wait(300);
        m_util.dropItemInBag(flaskBagCoord);
        m_util.wait(400);

        flask = findFlaskOrBucket();

        if (flask.q2 < 11) {
            m_util.sendErrorMessage("[GPS] Nothing to report...");
            m_util.stop = true;
            m_util.update();
            return;
        }

        m_util.sendMessage(String.format("[GPS] Found Q%d water", flask.q2));

        Pair<Double, Double> globalCoords;
        try {
            globalCoords = Geoloc.getPlayerCoords();
        } catch (GeolocException e) {
            m_util.sendErrorMessage("[GPS] Failed to get player coordinates");
            m_util.stop = true;
            m_util.update();
            return;
        }

        try {
            client.createResource(globalCoords.first, globalCoords.second, "water", flask.q2);
        } catch (MapAPIException e) {
            m_util.sendErrorMessage("[GPS] Failed to create resource on map");
        }

        m_util.stop = true;
        m_util.update();
    }

    Item findFlaskOrBucket() {
        Item flask = m_util.findFlask();
        if (flask == null) {
            flask = m_util.getItemFromBag("bucket");
        }

        if (flask == null) {
            m_util.sendErrorMessage("[GPS] Cannot find water flask or waterskin in inventory");
            m_util.stop = true;
            m_util.update();
        }

        return flask;
    }
}
