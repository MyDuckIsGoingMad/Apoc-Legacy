import java.util.ArrayList;
import java.util.Set;

import addons.*;
import haven.*;
import myduckisgoingmad.Storage;
import myduckisgoingmad.ui.WizardStep;
import myduckisgoingmad.ui.WizardWindow;
import myduckisgoingmad.ui.steps.GobSelectionStep;

/**
 * Example of BoulderDestroyer refactored to use the new Wizard framework. This
 * demonstrates how to create a multi-step script UI.
 */
public class BoulderDestroyerWizard extends Thread {
    public String scriptName = "Boulder Destroyer (Wizard)";

    HavenUtil m_util;
    Storage storage;
    int m_option;
    String m_modify;

    WizardWindow wizardWindow;

    public void ApocScript(HavenUtil util, int option, String modify) {
        m_util = util;
        storage = util.m_ui.storage;
        m_option = option;
        m_modify = modify;
    }

    protected void clear() {
        if (wizardWindow != null) {
            wizardWindow.destroy();
        }
    }

    public void run() {
        // Create the wizard window
        wizardWindow = new WizardWindow(new Coord(100, 100), UI.instance.root, scriptName) {
            @Override
            protected void onWizardComplete() {
                m_util.sendMessage("Starting boulder destruction...");
                // Execute the actual script
                executeBoulderDestruction();
            }

            @Override
            protected void onWizardCancelled() {
                m_util.sendMessage("Boulder destroyer cancelled.");
                m_util.forceStop();
            }
        };

        // Step 1: Select boulders
        GobSelectionStep selectionStep = new GobSelectionStep("Select Boulders", "gfx/terobjs/bumlings/02", true // require
                                                                                                                 // selection
        );
        wizardWindow.addStep(selectionStep);

        // Step 2: Configuration (if needed in future)
        ConfigurationStep configStep = new ConfigurationStep();
        wizardWindow.addStep(configStep);

        // Step 3: Confirmation
        ConfirmationStep confirmStep = new ConfirmationStep();
        wizardWindow.addStep(confirmStep);

        // Wait for wizard to complete
        while (!m_util.stop) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                m_util.sendErrorMessage(this.scriptName + " script interrupted: " + e.getMessage());
            }
        }

        clear();
        m_util.running(false);
    }

    private void executeBoulderDestruction() {
        // Get selected boulders from step 1
        GobSelectionStep selectionStep = (GobSelectionStep) wizardWindow.getStep(0);
        Set<Gob> selectedGobs = selectionStep.getSelectedGobs();

        if (selectedGobs == null || selectedGobs.isEmpty()) {
            m_util.sendErrorMessage("No boulders selected.");
            return;
        }

        m_util.startRunFlask();
        m_util.openInventory();
        m_util.wait(500);

        ArrayList<Gob> sortedBoulders = getSortedBouldersList(selectedGobs);

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
        m_util.forceStop();
    }

    private ArrayList<Gob> getSortedBouldersList(Set<Gob> gobs) {
        ArrayList<Gob> sortedBoulders = new ArrayList<Gob>(gobs);
        Coord playerCoord = m_util.getPlayerCoord();
        sortedBoulders.sort((g1, g2) -> {
            double dist1 = g1.getr().dist(playerCoord);
            double dist2 = g2.getr().dist(playerCoord);
            return Double.compare(dist1, dist2);
        });
        return sortedBoulders;
    }

    protected void destroyBoulder(Gob gob) {
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

    // Inner step classes for this specific script

    class ConfigurationStep extends WizardStep {
        public ConfigurationStep() {
            super("Configure Options");
        }

        @Override
        public void onInit() {
            new Label(new Coord(10, 10), this, "Configuration options will go here.");
            new Label(new Coord(10, 30), this, "For example:");
            new Label(new Coord(10, 50), this, "- Auto-drink when stamina is low");
            new Label(new Coord(10, 70), this, "- Drop stones automatically");
            new Label(new Coord(10, 90), this, "- etc.");
        }
    }

    class ConfirmationStep extends WizardStep {
        private Label summaryLabel;

        public ConfirmationStep() {
            super("Confirm and Start");
        }

        @Override
        public void onInit() {
            new Label(new Coord(10, 10), this, "Review your selections:");
            summaryLabel = new Label(new Coord(10, 40), this, "");
        }

        @Override
        public void onShow() {
            // Get data from previous steps
            GobSelectionStep selectionStep = (GobSelectionStep) getStep(0);
            Set<Gob> selectedGobs = selectionStep.getSelectedGobs();

            String summary = String.format("Boulders to destroy: %d", selectedGobs.size());
            summaryLabel.settext(summary);
        }

        @Override
        public boolean canProceed() {
            GobSelectionStep selectionStep = (GobSelectionStep) getStep(0);
            return !selectionStep.getSelectedGobs().isEmpty();
        }
    }
}
