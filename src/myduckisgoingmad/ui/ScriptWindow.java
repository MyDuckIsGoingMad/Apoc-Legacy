package myduckisgoingmad.ui;

import addons.HavenUtil;
import haven.Button;
import haven.Coord;
import haven.Label;
import haven.UI;
import haven.Widget;
import haven.WidgetFactory;
import haven.Window;
import myduckisgoingmad.utils.Helpers;

public class ScriptWindow extends Window {
	HavenUtil util;
	private String scriptName;
	private Button startButton;
	private Button stopButton;
	private Label elapsedTimeLabel;
	private long startTime;
	private long elapsedTime;
	private boolean isRunning;
	private long lastUpdate;
	private static final long UPDATE_INTERVAL = 1000; // Update every 1 second

	static {
		Widget.addtype("ui/scriptwindow", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				String scriptName = (String) args[0];
				// Runnable onStart = (Runnable) args[1];
				// Runnable onStop = (Runnable) args[2];
				return new ScriptWindow(c, parent, scriptName);
			}
		});
	}

	public void start() {
		//
	}

	public void stop() {
		//
	}

	public ScriptWindow(Coord c, Widget parent, String scriptName) {
		super(c, new Coord(200, 110), parent, scriptName);
		this.scriptName = scriptName;
		this.util = UI.instance.m_util;

		this.startTime = 0;
		this.elapsedTime = 0;
		this.isRunning = false;
		this.lastUpdate = 0;

		// Create start button
		this.startButton = new Button(new Coord(10, 10), 80, this, "Start") {
			public void click() {
				ScriptWindow.this.startTime = System.currentTimeMillis();
				ScriptWindow.this.isRunning = true;
				util.sendMessage(ScriptWindow.this.scriptName + " started.");
				ScriptWindow.this.start();
			}
		};

		// Create stop button
		this.stopButton = new Button(new Coord(100, 10), 80, this, "Stop") {
			public void click() {
				ScriptWindow.this.isRunning = false;
				util.sendMessage(ScriptWindow.this.scriptName + " stopped.");
				ScriptWindow.this.stop();
			}
		};

		// Create elapsed time label
		this.elapsedTimeLabel = new Label(new Coord(10, 50), this, "Elapsed: 00:00:00");
	}

	private void updateElapsedTimeLabel() {
		if (this.isRunning) {
			long currentElapsed = this.elapsedTime + (System.currentTimeMillis() - this.startTime);
			this.elapsedTimeLabel.settext("Elapsed: " + Helpers.formatTime(currentElapsed));
		}
	}

	@Override
	public void draw(haven.GOut g) {
		super.draw(g);

		// Update timer every UPDATE_INTERVAL
		long currentTime = System.currentTimeMillis();
		if (currentTime - this.lastUpdate >= UPDATE_INTERVAL) {
			this.lastUpdate = currentTime;
			updateElapsedTimeLabel();
		}
	}

	@Override
	public void destroy() {
		unlink();
		super.destroy();
	}

	public void uimsg(String msg, Object... args) {
		util.sendMessage("UI Msg: " + msg);
		super.uimsg(msg, args);
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		util.sendMessage("Widget Msg: " + msg);
		if (msg == "close") {
			this.isRunning = false;
			this.stop();
			// this.destroy();
		} else {
			super.wdgmsg(sender, msg, args);
		}
	}
}