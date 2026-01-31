package myduckisgoingmad.utils;

public enum ButtonCode {
    LEFT(1), MIDDLE(2), RIGHT(3), WHEEL_UP(4), WHEEL_DOWN(5);

    private final int code;

    ButtonCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
