import addons.*;
import haven.*;
import myduckisgoingmad.Storage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class FillTanningTubes extends Thread {
	public String scriptName = "Fill tanning tubes";

	HavenUtil m_util;
	Storage storage;
	int m_option;
	String m_modify;

	Gob well;
	List<Gob> tanningTubes;

	public void ApocScript(HavenUtil util, int option, String modify) {
		m_util = util;
		storage = util.m_ui.storage;
		m_option = option;
		m_modify = modify;
	}

	protected void clear() {
		if (well != null) {
			storage.clearHighlight(well);
		}

		if (tanningTubes != null) {
			for (Gob tube : tanningTubes) {
				storage.clearHighlight(tube);
			}
		}
	}

	Gob findWell() {
		well = m_util.findClosestObject("gfx/terobjs/well");
		if (well == null) {
			m_util.sendErrorMessage("No well found nearby.");
			m_util.stop = true;
		}
		storage.setHighlight(well, Color.CYAN);
		return well;
	}

	void findTanningTubes() {
		tanningTubes = m_util.getObjects("gfx/terobjs/ttub", 100);
		if (tanningTubes == null || tanningTubes.isEmpty()) {
			m_util.sendErrorMessage("No tanning tube found nearby.");
			m_util.stop = true;
			return;
		}
		m_util.sendMessage(String.format("Found %d tanning tubes", tanningTubes.size()));
		for (Gob tube : tanningTubes) {
			storage.setHighlight(tube, Color.RED);
		}
	}

	void fillBucketsFromWell() {
		ArrayList<Item> buckets = m_util.getItemsFromBag("buckete");
		Inventory bag = m_util.getInventory("Inventory");

		for (Item bucket : buckets) {
			if (m_util.stop)
				break;

			// Look for empty buckets
			if (bucket.GetResName().contains("buckete")) {
				// Pick up bucket
				bucket.wdgmsg("take", new Object[] { Coord.z });
				m_util.wait(200);

				// Use bucket on well
				m_util.itemActionWorldObject(well, 0);

				// Wait for bucket to fill
				int timeout = 0;
				while (!m_util.stop && timeout < 50) {
					if (m_util.getMouseItem() != null && m_util.getMouseItem().GetResName().contains("bucket-water")) {
						break;
					}
					m_util.wait(100);
					timeout++;
				}

				// Drop bucket back
				bag.drop(new Coord(0, 0), bucket.c);
				m_util.wait(200);
			}
		}

		// Clear hand if holding anything
		while (m_util.mouseHoldingAnItem() && !m_util.stop) {
			m_util.wait(100);
		}
	}

	boolean fillTubeFromBuckets(Gob tube) {
		ArrayList<Item> buckets = m_util.getItemsFromBag("bucket-water");
		Inventory bag = m_util.getInventory("Inventory");
		for (Item bucket : buckets) {
			if (m_util.stop)
				break;

			// Pick up bucket
			bucket.wdgmsg("take", new Object[] { Coord.z });
			m_util.wait(200);

			// Use bucket on tanning tube
			m_util.itemActionWorldObject(tube, 0);

			// Wait for action to complete
			int timeout = 0;
			while (!m_util.stop && timeout < 50) {
				if (m_util.getMouseItem() != null && m_util.getMouseItem().GetResName().contains("buckete")) {
					break;
				}
				m_util.wait(100);
				timeout++;
			}

			boolean stillHaveWater = !m_util.getMouseItem().GetResName().contains("buckete");

			// Drop bucket back
			bag.drop(new Coord(0, 0), bucket.c);
			m_util.wait(200);

			if (stillHaveWater) {
				return true;
			}
		}
		return false;
	}

	void fillTanningTube(Gob tube) {
		boolean full = false;

		while (!full && !m_util.stop) {
			m_util.walkTo(well);
			fillBucketsFromWell();
			m_util.walkTo(tube);
			full = fillTubeFromBuckets(tube);
			m_util.wait(500);
		}
	}

	public void run() {
		m_util.setPlayerSpeed(2);
		m_util.openInventory();
		m_util.wait(500);

		findWell();
		if (well == null) {
			return;
		}
		findTanningTubes();
		if (tanningTubes.isEmpty()) {
			clear();
			return;
		}
		for (Gob tube : tanningTubes) {
			storage.setHighlight(tube, Color.YELLOW);
			fillTanningTube(tube);
			storage.setHighlight(tube, Color.GREEN);
		}
		m_util.running(false);

		clear();
	}
}