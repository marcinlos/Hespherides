package hesp.traffic;

/**
 * Time distribution with fixed time difference between consecutive event
 * occurrences.
 * 
 * @author marcinlos
 */
public class FixedDelay implements TimeDistribution {
    
    private double delay;
    
    /**
     * Creates time distribution with given fixed delay.
     * 
     * @param delay
     */
    public FixedDelay(double delay) {
        this.delay = delay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double nextDelay() {
        return delay;
    }

}
