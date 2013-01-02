package hesp.traffic;

import java.util.Random;

/**
 * Implementation of {@link TimeDistribution} generating time differences
 * between events in Poisson process. 
 * 
 * @author marcinlos
 */
public class PoissonProces implements TimeDistribution {

    private Random random = new Random();
    private double lambda;
    
    /**
     * Creates new poisson process generator with given intensity.
     * 
     * @param lambda Intensity of events (unit: 1/s)
     */
    public PoissonProces(double lambda) {
        if (lambda <= 0) {
            throw new IllegalArgumentException("Invalid intensity " + 
                    "parameter: " + lambda);
        }
        this.lambda = lambda;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double nextDelay() {
        double u = random.nextDouble();
        return - Math.log(u) / lambda;
    }

}
