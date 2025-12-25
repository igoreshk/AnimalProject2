package Animal_Demykin.data;

public enum Command {
    ADD,
    LIST,
    EXIT,
    // ... остальные команды
    UNKNOWN;

    public static Command fromString(String text) {
        if (text == null) {
            return UNKNOWN;
        }
        try {
            return Command.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
