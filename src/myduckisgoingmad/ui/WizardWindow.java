package myduckisgoingmad.ui;

import java.util.ArrayList;
import java.util.List;

import haven.Button;
import haven.Coord;
import haven.Label;
import haven.UI;
import haven.Widget;
import haven.Window;

/**
 * A wizard-style window that displays multiple steps in sequence. Each step is
 * shown one at a time with Next/Previous/Finish navigation.
 */
public class WizardWindow extends Window {
    private List<WizardStep> steps;
    private int currentStepIndex;
    private Coord contentSize;

    // UI components
    private Label stepLabel;
    private Button previousButton;
    private Button nextButton;
    private Button finishButton;
    private Button cancelButton;

    private WizardStep currentStepWidget;

    /**
     * Create a new wizard window.
     * 
     * @param c           Position
     * @param parent      Parent widget
     * @param title       Window title
     * @param contentSize Size of the content area (where steps are displayed)
     */
    public WizardWindow(Coord c, Widget parent, String title, Coord contentSize) {
        super(c, contentSize.add(0, 90), parent, title); // Add space for buttons and label
        this.contentSize = contentSize;
        this.steps = new ArrayList<>();
        this.currentStepIndex = 0;

        initUI();
    }

    /**
     * Convenience constructor with default content size.
     */
    public WizardWindow(Coord c, Widget parent, String title) {
        this(c, parent, title, new Coord(400, 300));
    }

    private void initUI() {
        // Step label at the top
        stepLabel = new Label(new Coord(10, 10), this, "");

        // Buttons at the bottom
        int buttonY = contentSize.y + 10;

        previousButton = new Button(new Coord(10, buttonY), 80, this, "Previous") {
            public void click() {
                previousStep();
            }
        };

        nextButton = new Button(new Coord(100, buttonY), 80, this, "Next") {
            public void click() {
                nextStep();
            }
        };

        finishButton = new Button(new Coord(190, buttonY), 80, this, "Finish") {
            public void click() {
                finish();
            }
        };

        cancelButton = new Button(new Coord(280, buttonY), 80, this, "Cancel") {
            public void click() {
                cancel();
            }
        };

        updateNavigationButtons();
    }

    /**
     * Add a step to the wizard. Steps are displayed in the order they are added.
     */
    public void addStep(WizardStep step) {
        step.setWizard(this);
        steps.add(step);

        // Initialize the first step if this is it
        if (steps.size() == 1) {
            showStep(0);
        }
    }

    /**
     * Show a specific step by index.
     */
    private void showStep(int index) {
        if (index < 0 || index >= steps.size()) {
            return;
        }

        // Hide current step
        if (currentStepWidget != null) {
            if (!currentStepWidget.onHide()) {
                return; // Step prevented navigation
            }
            currentStepWidget.unlink();
        }

        // Show new step
        currentStepIndex = index;
        WizardStep step = steps.get(index);

        // Create a new instance in the window
        currentStepWidget = step;
        currentStepWidget.parent = this;
        currentStepWidget.c = new Coord(10, 35); // Below the step label

        // Get preferred size or use default
        Coord stepSize = step.getPreferredSize();
        if (stepSize != null) {
            currentStepWidget.sz = stepSize;
        } else {
            currentStepWidget.sz = contentSize;
        }

        link();

        // Initialize if first time showing
        if (!step.isVisible()) {
            step.onInit();
            step.show();
        }

        step.onShow();

        // Update UI
        updateStepLabel();
        updateNavigationButtons();
    }

    private void updateStepLabel() {
        WizardStep step = steps.get(currentStepIndex);
        stepLabel.settext(String.format("Step %d of %d: %s", currentStepIndex + 1, steps.size(), step.getStepTitle()));
    }

    private void updateNavigationButtons() {
        if (steps.isEmpty()) {
            previousButton.hide();
            nextButton.hide();
            finishButton.hide();
            return;
        }

        previousButton.setVisible(currentStepIndex > 0);
        nextButton.setVisible(currentStepIndex < steps.size() - 1);
        finishButton.setVisible(currentStepIndex == steps.size() - 1);
    }

    /**
     * Navigate to the previous step.
     */
    public void previousStep() {
        if (currentStepIndex > 0) {
            showStep(currentStepIndex - 1);
        }
    }

    /**
     * Navigate to the next step.
     */
    public void nextStep() {
        if (currentStepIndex < steps.size() - 1) {
            WizardStep currentStep = steps.get(currentStepIndex);
            if (!currentStep.canProceed()) {
                UI.instance.m_util.sendErrorMessage("Please complete the current step before proceeding.");
                return;
            }
            showStep(currentStepIndex + 1);
        }
    }

    /**
     * Finish the wizard. Calls onFinish() on all steps.
     */
    public void finish() {
        WizardStep currentStep = steps.get(currentStepIndex);
        if (!currentStep.canProceed()) {
            UI.instance.m_util.sendErrorMessage("Please complete all required fields.");
            return;
        }

        // Call onFinish for all steps
        for (WizardStep step : steps) {
            step.onFinish();
        }

        onWizardComplete();
        destroy();
    }

    /**
     * Cancel the wizard without finishing.
     */
    public void cancel() {
        onWizardCancelled();
        destroy();
    }

    /**
     * Override this method to handle wizard completion.
     */
    protected void onWizardComplete() {
        // Default: do nothing
    }

    /**
     * Override this method to handle wizard cancellation.
     */
    protected void onWizardCancelled() {
        // Default: do nothing
    }

    /**
     * Get a step by index.
     */
    public WizardStep getStep(int index) {
        if (index >= 0 && index < steps.size()) {
            return steps.get(index);
        }
        return null;
    }

    /**
     * Get the current step.
     */
    public WizardStep getCurrentStep() {
        return steps.get(currentStepIndex);
    }

    /**
     * Get the total number of steps.
     */
    public int getStepCount() {
        return steps.size();
    }
}
