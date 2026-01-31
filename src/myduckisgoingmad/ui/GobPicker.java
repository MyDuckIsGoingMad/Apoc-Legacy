package myduckisgoingmad.ui;

import java.util.List;
import java.util.Set;

import addons.HavenUtil;
import haven.Button;
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

	public Set<Gob> selectedGobs;
	private boolean multiple = true;
	private String filter;

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

		new Button(new Coord(10, 16), 80, this, "Clear") {
			public void click() {
				GobPicker.this.clear();
			}
		};

		new Button(new Coord(100, 16), 80, this, "Select") {
			public void click() {
				if (multiple) {
					select(selectedGobs);
				} else {
					if (selectedGobs.size() > 0) {
						select(selectedGobs.iterator().next());
					}
				}

				GobPicker.this.destroy();
			}
		};

		oldHighlight = Config.highlight;
		Config.highlight = true;
		selectedGobs = new java.util.HashSet<Gob>();
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

	private void processGob(Gob gob) {
		if (filter != null && !filter.isEmpty() && !gob.resname().contains(filter)) {
			return;
		}

		util.sendMessage(String.format("Process gob: %s", gob.resname()));
		if (selectedGobs.contains(gob)) {
			util.m_ui.storage.clearHighlight(gob);
			selectedGobs.remove(gob);
		} else {
			if (!multiple && !selectedGobs.isEmpty()) {
				clear();
			}
			util.m_ui.storage.setHighlight(gob, java.awt.Color.MAGENTA);
			this.selectedGobs.add(gob);
		}
		updateText();
	}

	public void clear() {
		if (selectedGobs != null) {
			for (Gob gob : selectedGobs) {
				util.m_ui.storage.clearHighlight(gob);
			}
			selectedGobs.clear();
			updateText();
		}
	}

	private void updateText() {
		this.totalItemsLabel.settext(String.format(GobPicker.totalItemsFmt, selectedGobs.size()));
	}

	public void select(Gob gob) {
		//
	}

	public void select(Set<Gob> gobs) {
		//
	}

	@Override
	public void mmousedown(Coord mc, int button) {
		util.sendMessage(String.format("Button clicked: %d", button));

		if (MapView.gobAtMouse != null) {
			processGob(MapView.gobAtMouse);
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

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
}