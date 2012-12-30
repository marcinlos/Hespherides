package hesp.util;

/**
 * @author marcinlos
 * 
 * Message structure for simple logging mechanism.
 */
public class LogItem {

    /**
     * Enum representing importance and nature of a log message.
     */
    public enum Level {
        INFO, 
        ERROR, 
        SUCCESS
    }

    private String text;
    private Level level;

    /**
     * @return log item with level {@code INFO} containing {@code text}
     */
    public static LogItem info(String text) {
        return new LogItem(text, Level.INFO);
    }
    
    /**
     * @return log item with level {@code SUCCESS} containing {@code text}
     */
    public static LogItem success(String text) {
        return new LogItem(text, Level.SUCCESS);
    }
    
    /**
     * @return log item with level {@code SUCCESS} containing {@code text}
     */
    public static LogItem error(String text) {
        return new LogItem(text, Level.ERROR);
    }

    
    public LogItem(String text, Level level) {
        this.text = text;
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

}
