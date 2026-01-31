import java.util.ArrayList;
import java.util.Set;

import addons.*;
import haven.*;
import myduckisgoingmad.Storage;
import myduckisgoingmad.ui.GobPicker;
import myduckisgoingmad.ui.ScriptWindow;

public class BoulderDestroyer extends Thread {
	public String scriptName = "Boulder Destroyer";

	HavenUtil m_util;
	Storage storage;
	int m_option;
	String m_modify;

	ScriptWindow scriptWnd;
	GobPicker pickerWnd;
	Set<Gob> selectedGobs;
	boolean startDestruction = false;

	public void ApocScript(HavenUtil util, int option, String modify) {
		m_util = util;
		storage = util.m_ui.storage;
		m_option = option;
		m_modify = modify;
	}

	protected void clear() {
		if (scriptWnd != null) {
			scriptWnd.destroy();
		}
		if (pickerWnd != null) {
			pickerWnd.destroy();
		}
	}

	private ArrayList<Gob> getSortedBouldersList() {
		ArrayList<Gob> sortedBoulders = new ArrayList<Gob>(selectedGobs);
		Coord playerCoord = m_util.getPlayerCoord();
		sortedBoulders.sort((g1, g2) -> {
			double dist1 = g1.getr().dist(playerCoord);
			double dist2 = g2.getr().dist(playerCoord);
			return Double.compare(dist1, dist2);
		});
		return sortedBoulders;
	}

	protected void destroyBoulders() {
		if (selectedGobs == null || selectedGobs.isEmpty()) {
			m_util.sendErrorMessage("No boulders selected.");
			return;
		}

		ArrayList<Gob> sortedBoulders = getSortedBouldersList();

		for (Gob boulder : sortedBoulders) {
			storage.setHighlight(boulder, java.awt.Color.YELLOW);
		}

		for (Gob boulder : sortedBoulders) {
			if (m_util.stop)
				break;

			if (!m_util.findObject(boulder)) {
				m_util.sendMessage("Boulder no longer exists, skipping...");
				storage.clearHighlight(boulder);
				continue;
			}

			storage.setHighlight(boulder, java.awt.Color.RED);

			destroyBoulder(boulder);

			storage.clearHighlight(boulder);
		}

		m_util.sendMessage("Boulder destruction complete.");
	}

	protected void destroyBoulder(Gob gob) {
		m_util.sendMessage("Destroy boulder: " + gob);
		while (!m_util.stop && m_util.findObject(gob)) {
			m_util.walkTo(gob);
			if (m_util.stop)
				return;

			m_util.clickWorldObject(3, gob);
			m_util.wait(100);
			m_util.autoFlowerMenu("Chip stone");

			while (!m_util.hasHourglass() && !m_util.stop) {
				m_util.wait(100);
			}
			if (m_util.stop)
				return;

			while (m_util.hasHourglass() && !m_util.stop) {
				m_util.wait(100);
			}
			if (m_util.stop)
				return;

			if (m_util.getPlayerBagSpace() == 0) {
				m_util.sendMessage("Inventory full, dropping stones...");
				dropAllStones();
			}
		}
	}

	protected void dropAllStones() {
		ArrayList<Item> bagItems = m_util.getItemsFromBag();
		for (Item item : bagItems) {
			if (item.GetResName().contains("gfx/invobjs/stone")) {
				m_util.dropItemOnGround(item);
			}
		}
		m_util.wait(200);
	}

	public void run() {
		scriptWnd = new ScriptWindow(new Coord(250, 100), UI.instance.root, this.scriptName) {
			@Override
			public void start() {
				if (BoulderDestroyer.this.selectedGobs != null && !BoulderDestroyer.this.selectedGobs.isEmpty()) {
					startDestruction = true;
					m_util.sendMessage("Starting boulder destruction...");
				} else {
					m_util.sendErrorMessage("Please select boulders first!");
				}
			}

			@Override
			public void stop() {
				m_util.forceStop();
			}
		};

		pickerWnd = new GobPicker(new Coord(500, 100), UI.instance.root) {
			@Override
			public void select(Set<Gob> gobs) {
				m_util.sendMessage("Selected " + gobs.size() + " gobs for Boulder Destroyer.");
				BoulderDestroyer.this.selectedGobs = gobs;
			}
		};

		pickerWnd.setMultiple(true);
		pickerWnd.setFilter("gfx/terobjs/bumlings/02");

		while (!m_util.stop) {
			if (startDestruction) {
				startDestruction = false;
				destroyBoulders();
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				m_util.sendErrorMessage(this.scriptName + " script interrupted: " + e.getMessage());
			}
		}
		clear();
		m_util.running(false);
	}
}