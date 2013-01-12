package hesp.traffic;

/**
 * Interface of a time distribution provider. Intended mainly for the use 
 * of GUI components.
 * 
 * @author marcinlos
 * @see TimeDistribution
 * @see JobGeneratorProvider
 */
public interface TimeDistributionProvider {
    
    /**
     * @return a time distribution ({@code TimeDistribution} implementation)
     */
    TimeDistribution getDistribution();

}
