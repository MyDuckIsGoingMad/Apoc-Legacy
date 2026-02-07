package myduckisgoingmad.ui;

import haven.Coord;
import haven.Widget;

/**
 * Base class for wizard steps. Each step represents a panel in the wizard.
 * Steps are responsible for their own UI and validation logic.
 */
public abstract class WizardStep extends Widget {
    protected WizardWindow wizard;
    protected String stepTitle;

    public WizardStep(String stepTitle) {
        super(Coord.z, Coord.z, null);
        this.stepTitle = stepTitle;
    }

    /**
     * Called when the step is initialized and added to the wizard. Override to set
     * up UI components.
     */
    public abstract void onInit();

    /**
     * Called when the step becomes visible (user navigates to it). Override to
     * refresh data or update UI based on previous steps.
     */
    public void onShow() {
        // Default: do nothing
    }

    /**
     * Called when the step is about to be hidden (user navigates away). Return
     * false to prevent navigation (e.g., if validation fails).
     */
    public boolean onHide() {
        return true;
    }

    /**
     * Called when the wizard is finished (Finish button clicked on last step).
     * Override to perform final actions.
     */
    public void onFinish() {
        // Default: do nothing
    }

    /**
     * Set the wizard reference. Called automatically by WizardWindow.
     */
    void setWizard(WizardWindow wizard) {
        this.wizard = wizard;
    }

    /**
     * Get the title of this step.
     */
    public String getStepTitle() {
        return stepTitle;
    }

    /**
     * Override to provide a custom size for this step's content area. Return null
     * to use the wizard's default size.
     */
    public Coord getPreferredSize() {
        return null;
    }

    /**
     * Check if this step can proceed to the next step. Override to add validation
     * logic.
     */
    public boolean canProceed() {
        return true;
    }

    /**
     * Get data from previous steps. Useful for passing information between steps.
     */
    protected WizardStep getStep(int index) {
        return wizard.getStep(index);
    }
}
