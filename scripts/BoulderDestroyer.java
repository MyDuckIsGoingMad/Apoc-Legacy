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

	public void run() {
		scriptWnd = new ScriptWindow(new Coord(250, 100), UI.instance.root, this.scriptName) {
			public void start() {
				//
			}

			public void stop() {
				m_util.forceStop();
			}
		};

		pickerWnd = new GobPicker(new Coord(500, 100), UI.instance.root) {
			//
		};

		while (!m_util.stop) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				m_util.sendErrorMessage(this.scriptName + " script interrupted: " + e.getMessage());
			}
		}
		clear();
		m_util.running(false);
	}
}