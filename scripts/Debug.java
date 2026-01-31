import addons.*;
import haven.*;
import myduckisgoingmad.Storage;
import myduckisgoingmad.ui.ScriptWindow;

public class Debug extends Thread {
	public String scriptName = "Debug";

	HavenUtil m_util;
	Storage storage;
	int m_option;
	String m_modify;

	ScriptWindow wnd;

	public void ApocScript(HavenUtil util, int option, String modify) {
		m_util = util;
		storage = util.m_ui.storage;
		m_option = option;
		m_modify = modify;
	}

	protected void clear() {
		if (wnd != null) {
			wnd.destroy();
		}
	}

	public void run() {
		m_util.sendMessage("Debug script is running...");
		wnd = new ScriptWindow(new Coord(250, 100), UI.instance.root, this.scriptName) {
			public void start() {
				m_util.sendMessage("Debug script started.");
			}

			public void stop() {
				m_util.sendMessage("Debug script stopped.");
				m_util.forceStop();
			}
		};

		while (!m_util.stop) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				m_util.sendErrorMessage("Debug script interrupted: " + e.getMessage());
			}
		}
		clear();
		m_util.running(false);
	}
}