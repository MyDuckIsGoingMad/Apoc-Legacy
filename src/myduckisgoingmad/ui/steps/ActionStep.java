package myduckisgoingmad.ui.steps;

import haven.Button;
import haven.Coord;
import haven.Label;
import haven.UI;
import myduckisgoingmad.ui.WizardStep;

/**
 * A reusable confirmation/action step for wizard flows. Displays a message and
 * provides a way to trigger an action.
 */
public class ActionStep extends WizardStep {
    private String message;
    private String actionButtonText;
    private Runnable action;
    private boolean actionExecuted;
    private Label statusLabel;

    public ActionStep(String stepTitle, String message, String actionButtonText) {
        super(stepTitle);
        this.message = message;
        this.actionButtonText = actionButtonText;
        this.actionExecuted = false;
    }

    @Override
    public void onInit() {
        // Message
        new Label(new Coord(10, 10), this, message);

        // Action button (if action is set)
        if (actionButtonText != null) {
            new Button(new Coord(10, 50), 150, this, actionButtonText) {
                public void click() {
                    executeAction();
                }
            };
        }

        // Status label
        statusLabel = new Label(new Coord(10, 90), this, "");
    }

    @Override
    public void onShow() {
        if (actionExecuted) {
            statusLabel.settext("Action completed");
        }
    }

    /**
     * Set the action to be executed when the button is clicked.
     */
    public void setAction(Runnable action) {
        this.action = action;
    }

    private void executeAction() {
        if (action != null) {
            try {
                action.run();
                actionExecuted = true;
                statusLabel.settext("Action completed successfully");
                UI.instance.m_util.sendMessage(getStepTitle() + " action completed.");
            } catch (Exception e) {
                statusLabel.settext("Action failed: " + e.getMessage());
                UI.instance.m_util.sendErrorMessage("Action failed: " + e.getMessage());
            }
        }
    }

    /**
     * Check if the action has been executed.
     */
    public boolean isActionExecuted() {
        return actionExecuted;
    }
}
