# Wizard UI Framework

A flexible, reusable UI framework for creating multi-step wizard interfaces in Haven and Hearth scripts.

## Overview

The Wizard UI Framework provides a structured way to create multi-step user interfaces where each step is a self-contained, reusable component. This is perfect for scripts that need to:
- Collect multiple inputs from the user
- Guide users through a process step-by-step
- Provide clear navigation between different configuration screens

## Key Components

### 1. WizardWindow
The main container that holds and manages wizard steps.

**Features:**
- Automatic step navigation (Previous/Next/Finish)
- Step counter display ("Step 1 of 3")
- Cancel support
- Validation before proceeding to next step

### 2. WizardStep
Abstract base class for creating individual wizard steps.

**Override methods:**
- `onInit()` - Set up UI components (called once)
- `onShow()` - Refresh data when step is shown
- `onHide()` - Clean up or validate before leaving (return false to block navigation)
- `canProceed()` - Validate before moving to next step
- `onFinish()` - Final actions when wizard completes

### 3. Reusable Steps
Pre-built step implementations in `myduckisgoingmad.ui.steps`:

- **GobSelectionStep** - Select game objects from the world
- **ActionStep** - Display message and execute an action

## Quick Start Example

```java
// Create the wizard window
WizardWindow wizard = new WizardWindow(new Coord(100, 100), UI.instance.root, "My Script") {
    @Override
    protected void onWizardComplete() {
        // Execute your script logic here
        executeScript();
    }
    
    @Override
    protected void onWizardCancelled() {
        m_util.sendMessage("Script cancelled.");
    }
};

// Add steps
GobSelectionStep step1 = new GobSelectionStep("Select Objects", "gfx/terobjs/tree", true);
wizard.addStep(step1);

ConfigStep step2 = new ConfigStep();
wizard.addStep(step2);

// Access data from previous steps
class ConfigStep extends WizardStep {
    public ConfigStep() {
        super("Configure");
    }
    
    @Override
    public void onInit() {
        new Label(new Coord(10, 10), this, "Configure options...");
    }
    
    @Override
    public void onShow() {
        // Get data from step 1
        GobSelectionStep selectionStep = (GobSelectionStep) getStep(0);
        Set<Gob> selected = selectionStep.getSelectedGobs();
        // Use the selected objects...
    }
}
```

## Creating Custom Steps

```java
public class MyCustomStep extends WizardStep {
    private TextEntry nameInput;
    
    public MyCustomStep() {
        super("Enter Name");
    }
    
    @Override
    public void onInit() {
        new Label(new Coord(10, 10), this, "Enter your name:");
        nameInput = new TextEntry(new Coord(10, 30), 200, this, "");
    }
    
    @Override
    public boolean canProceed() {
        // Validate input
        return nameInput.text.length() > 0;
    }
    
    public String getName() {
        return nameInput.text;
    }
}
```

## Design Principles

1. **Separation of Concerns** - Each step handles its own UI and logic
2. **Reusability** - Steps can be used across different scripts
3. **Simplicity** - Easy to understand and use
4. **Extensibility** - Can be enhanced with more features as needed

## Future Enhancements

Potential improvements for later iterations:
- Conditional navigation (skip steps based on user choices)
- Step history/breadcrumbs
- Dynamic step insertion/removal
- Step templates for common patterns
- Integration with existing UI components (GobPicker, etc.)

## File Structure

```
src/myduckisgoingmad/ui/
├── WizardWindow.java      # Main wizard container
├── WizardStep.java        # Abstract base class for steps
└── steps/                 # Reusable step implementations
    ├── GobSelectionStep.java
    └── ActionStep.java
```

## Example Scripts

See `scripts/BoulderDestroyerWizard.java` for a complete example of refactoring an existing script to use the wizard framework.
