package hesp.traffic;

import hesp.protocol.JobParameters;

import java.util.Random;

/**
 * {@link JobGenerator} implementation creating jobs with uniformly distributed
 * CPU time on a given interval.
 * 
 * @author marcinlos
 */
public class UniformJobGenerator implements JobGenerator {

    private int minimum;
    private int maixmum;
    private Random random = new Random();

    /**
     * Creates a job generator with CPU time distribution {@code 
     * ~U(minimum, maximum)}
     * 
     * @param minimum Lower bound
     * @param maximum Upper bound
     */
    public UniformJobGenerator(int minimum, int maximum) {
        this.minimum = minimum;
        this.maixmum = maximum;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobParameters nextJob() {
        int cputime = minimum + random.nextInt(maixmum - minimum + 1);
        return new JobParameters(cputime);
    }
    
}
