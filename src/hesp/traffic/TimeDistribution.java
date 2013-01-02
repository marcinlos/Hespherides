package hesp.traffic;

/**
 * Interface of general generator of delay times between events.
 * 
 * @author marcinlos
 */
public interface TimeDistribution {
    
    /**
     * @return amount (in seconds) of time between next occurrence of an event
     */
    double nextDelay();

}
