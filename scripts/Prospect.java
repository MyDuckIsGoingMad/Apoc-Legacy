import java.util.ArrayList;

import addons.*;
import haven.*;

public class Prospect extends Thread {
	public String scriptName = "Prospect";
	public String[] options = {};

	HavenUtil m_util;
	int m_option;
	String m_modify;

	public void ApocScript(HavenUtil util, int option, String modify) {
		m_util = util;
		m_option = option;
		m_modify = modify;
	}

	public void run() {
		prospect();
		m_util.running(false);
	}

	private void prospect() {
		m_util.sendMessage("[GPS] Prospecting ...");
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
			return;
		}
		for (Item item : newItems) {
			if (!items.contains(item)) {
				m_util.sendMessage("[GPS] Found item: " + item.GetResName() + ". Quality: " + item.q);
			}
		}
	}
}