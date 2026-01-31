package myduckisgoingmad.utils;

public enum MouseButton {
    LEFT(1), MIDDLE(2), RIGHT(3), BACK(4), FORWARD(5);

    private final int buttonCode;

    MouseButton(int buttonCode) {
        this.buttonCode = buttonCode;
    }

    public int getButtonCode() {
        return buttonCode;
    }

    public static MouseButton fromCode(int code) {
        for (MouseButton button : values()) {
            if (button.buttonCode == code) {
                return button;
            }
        }
        throw new IllegalArgumentException("Unknown mouse button code: " + code);
    }
}
