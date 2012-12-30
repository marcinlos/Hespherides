package hesp.util;

import hesp.util.LogItem.Level;

/**
 * @author marcinlos
 *
 * Skeletal implementation of generic log sink, delegating all the operations
 * to {@link LogSink#log(LogItem)} method.
 */
abstract public class AbstractLogSink implements LogSink {

    @Override
    public void log(String text, Level level) {
        log(new LogItem(text, level));
    }
    
    @Override
    public void info(String text) {
        log(LogItem.info(text));
    }
    
    @Override
    public void success(String text) {
        log(LogItem.success(text));
    }
    
    @Override
    public void error(String text) {
        log(LogItem.error(text));
    }
    
}
