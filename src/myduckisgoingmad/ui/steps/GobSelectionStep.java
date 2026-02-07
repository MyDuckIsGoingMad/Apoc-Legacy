package myduckisgoingmad.ui.steps;

import java.util.Set;

import haven.Button;
import haven.Coord;
import haven.Gob;
import haven.Label;
import haven.UI;
import myduckisgoingmad.ui.WizardStep;

/**
 * A reusable wizard step for selecting game objects (Gobs). Provides a UI for
 * picking multiple objects from the game world.
 */
public class GobSelectionStep extends WizardStep {
    private String filter;
    private Set<Gob> selectedGobs;
    private Label countLabel;
    private boolean requireSelection;

    public GobSelectionStep(String stepTitle, String filter, boolean requireSelection) {
        super(stepTitle);
        this.filter = filter;
        this.requireSelection = requireSelection;
        this.selectedGobs = new java.util.HashSet<>();
    }

    @Override
    public void onInit() {
        // Instructions
        new Label(new Coord(10, 10), this, "Click objects in the game world to select them.");
        new Label(new Coord(10, 30), this, "Filter: " + (filter != null ? filter : "All objects"));

        // Count label
        countLabel = new Label(new Coord(10, 60), this, "Selected: 0");

        // Clear button
        new Button(new Coord(10, 90), 100, this, "Clear Selection") {
            public void click() {
                clearSelection();
            }
        };

        // Open picker button
        new Button(new Coord(120, 90), 100, this, "Pick from Map") {
            public void click() {
                openGobPicker();
            }
        };
    }

    @Override
    public void onShow() {
        updateCountLabel();
    }

    @Override
    public boolean canProceed() {
        if (requireSelection && selectedGobs.isEmpty()) {
            return false;
        }
        return true;
    }

    private void openGobPicker() {
        // This would integrate with your existing GobPicker
        // For now, just show a message
        UI.instance.m_util.sendMessage("Gob picker functionality - to be integrated");
    }

    private void clearSelection() {
        selectedGobs.clear();
        updateCountLabel();
        UI.instance.m_util.sendMessage("Selection cleared.");
    }

    private void updateCountLabel() {
        countLabel.settext("Selected: " + selectedGobs.size());
    }

    /**
     * Add a gob to the selection.
     */
    public void addGob(Gob gob) {
        selectedGobs.add(gob);
        updateCountLabel();
    }

    /**
     * Get the selected gobs.
     */
    public Set<Gob> getSelectedGobs() {
        return selectedGobs;
    }

    /**
     * Set the selected gobs (useful for pre-populating).
     */
    public void setSelectedGobs(Set<Gob> gobs) {
        this.selectedGobs = gobs;
        updateCountLabel();
    }
}
