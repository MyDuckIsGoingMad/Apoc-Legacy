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

		m_util.startRunFlask();

		m_util.openInventory();
		m_util.wait(500);

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
		while (!m_util.stop && m_util.findObject(gob)) {
			// Check and manage stamina
			// if (!checkAndManageStamina()) {
			// return;
			// }

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

	protected boolean checkAndManageStamina() {
		double stamina = m_util.getStamina();

		if (stamina < 30) {
			m_util.sendMessage("Stamina low (" + String.format("%.1f", stamina) + "%), attempting to drink...");

			// Find flask in inventory
			Item flask = m_util.findFlask();
			if (flask == null) {
				m_util.sendErrorMessage("No flask found in inventory! Cannot continue with low stamina.");
				m_util.forceStop();
				return false;
			}

			// Try to drink from flask
			m_util.clickBagItem(flask, 1);
			m_util.wait(500);

			// Check if stamina improved
			double newStamina = m_util.getStamina();
			if (newStamina > stamina + 5) {
				m_util.sendMessage("Drank from flask, stamina restored.");
				return true;
			}

			// Flask might be empty, try to fill it from bucket
			m_util.sendMessage("Flask appears empty, looking for water bucket...");
			ArrayList<Item> bagItems = m_util.getItemsFromBag();
			Item waterBucket = null;

			for (Item item : bagItems) {
				String resName = item.GetResName();
				if (resName.contains("gfx/invobjs/bucket")) {
					// Check if bucket has water (has contents)
					// if (item.contents != null && !item.contents.isEmpty()) {
					// waterBucket = item;
					// break;
					// }
				}
			}

			if (waterBucket != null) {
				m_util.sendMessage("Found water bucket, filling flask...");
				// Click bucket with flask to fill
				// m_util.takeItem(flask);
				// m_util.wait(200);
				// m_util.clickItem(waterBucket, 1);
				// m_util.wait(500);

				// // Now try drinking again
				// m_util.clickItem(flask, 1);
				// m_util.wait(500);
				// m_util.sendMessage("Flask filled and consumed.");
				return true;
			} else {
				m_util.sendErrorMessage("No water available! Stamina too low to continue. Stopping script.");
				m_util.forceStop();
				return false;
			}
		}

		return true;
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