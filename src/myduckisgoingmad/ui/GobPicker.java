package myduckisgoingmad.ui;

import java.util.List;

import addons.HavenUtil;
import haven.Config;
import haven.Coord;
import haven.Gob;
import haven.Label;
import haven.UI;
import haven.MapView;
import haven.MapView.Grabber;
import haven.Widget;
import haven.WidgetFactory;
import haven.Window;

public class GobPicker extends Window implements Grabber {
	private static final String totalItemsFmt = "Items selected: %d";
	private Label totalItemsLabel;

	private HavenUtil util;
	private boolean oldHighlight;

	public List<Gob> selectedGobs;

	static {
		Widget.addtype("ui/gobpicker", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return new GobPicker(c, parent);
			}
		});
	}

	public GobPicker(Coord c, Widget parent) {
		super(c, new Coord(200, 30), parent, "Gob Picker");
		this.util = UI.instance.m_util;
		this.ui.mainview.grab(this);
		this.totalItemsLabel = new Label(Coord.z, this, String.format(GobPicker.totalItemsFmt, 0));
		oldHighlight = Config.highlight;
		Config.highlight = true;
		selectedGobs = new java.util.ArrayList<Gob>();
	}

	@Override
	public void destroy() {
		Config.highlight = oldHighlight;

		if (selectedGobs != null) {
			for (Gob gob : selectedGobs) {
				util.m_ui.storage.clearHighlight(gob);
			}
		}

		this.ui.mainview.release(this);
		unlink();
		super.destroy();
	}

	@Override
	public void mmousedown(Coord mc, int button) {
		util.sendMessage(String.format("Button clicked: %d", button));
		Gob mouseGob = MapView.gobAtMouse;

		if (mouseGob != null) {
			util.sendMessage(String.format("Gob at mouse: %s", mouseGob.resname()));
			util.m_ui.storage.setHighlight(mouseGob, java.awt.Color.MAGENTA);
			this.selectedGobs.add(mouseGob);
		} else {
			util.sendMessage("No gob at mouse");
		}
	}

	@Override
	public void mmouseup(Coord mc, int button) {
		//
	}

	@Override
	public void mmousemove(Coord mc) {
		//
	}

	@Override
	public void uimsg(String msg, Object... args) {
		if (msg == "reset") {
			//
		}
	}
}