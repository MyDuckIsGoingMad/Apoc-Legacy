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

public class Prospector extends Thread {
    HavenUtil m_util;
    MapAPIClient client;

    public Prospector(MapAPIClient client, HavenUtil util) {
        this.client = client;
        this.m_util = util;
    }

    public void run() {
        m_util.stop = false;
        m_util.update();
        m_util.sendMessage("[GPS] Prospecting ...");
        Pair<Double, Double> globalCoords;
        try {
            globalCoords = Geoloc.getPlayerCoords();
        } catch (GeolocException e) {
            m_util.sendErrorMessage("[GPS] Failed to get player coordinates");
            m_util.stop = true;
            m_util.update();
            return;
        }
        m_util.openInventory();
        Inventory bag = m_util.getInventory("Inventory");

        if (bag == null) {
            m_util.sendErrorMessage("Cannot find player inventory");
            return;
        }
        ArrayList<Item> items = m_util.getItemsFromBag();
        Coord playerCoord = m_util.getPlayerCoord();

        int itemsCount = m_util.invItemCount(bag);
        m_util.sendAction("dig");
        m_util.clickWorld(1, playerCoord);
        while (!m_util.hasHourglass() && !m_util.stop) {
            m_util.wait(100);
        }
        while (m_util.hasHourglass() && !m_util.stop) {
            m_util.wait(100);
            if (m_util.invItemCount(bag) > itemsCount) {
                m_util.clickWorld(3, playerCoord);
                m_util.clickWorld(1, playerCoord);
                break;
            }
        }

        ArrayList<Item> newItems = m_util.getItemsFromBag();

        if (newItems.size() <= items.size()) {
            m_util.sendMessage("[GPS] Prospecting failed.");
            m_util.stop = true;
            m_util.update();
            return;
        }

        Item foundItem = null;
        for (Item item : newItems) {
            if (!items.contains(item)) {
                foundItem = item;
                break;
            }
        }

        if (foundItem != null) {
            String resourceType = getResourceType(foundItem);
            if (resourceType == null) {
                m_util.sendMessage(
                        "[GPS] Found unknown resource: " + foundItem.GetResName() + ". Quality: " + foundItem.q);
            } else {
                m_util.sendMessage(String.format("[GPS] Found %s: %s. Quality: %d", resourceType,
                        foundItem.GetResName(), foundItem.q));
                try {
                    client.createResource(globalCoords.first, globalCoords.second, resourceType, foundItem.q);
                } catch (MapAPIException e) {
                    m_util.sendErrorMessage("[GPS] Failed to create resource on map");
                }
            }
            m_util.dropItemOnGround(foundItem);
        }

        m_util.stop = true;
        m_util.update();
    }

    private String getResourceType(Item item) {
        String resname = item.GetResName();
        if (resname.contains("/invobjs/soil") || resname.contains("/invobjs/earthworm")) {
            return "soil";
        }
        if (resname.contains("/invobjs/sand")) {
            return "sand";
        }
        if (resname.contains("/invobjs/clay")) {
            return "clay";
        }
        if (resname.contains("/invobjs/stone")) {
            return "stone";
        }
        if (resname.contains("/invobjs/clay-acre")) {
            return "clay_acre";
        }
        return null;
    }

}
