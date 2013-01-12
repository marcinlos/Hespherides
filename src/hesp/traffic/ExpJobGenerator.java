package hesp.traffic;

import hesp.protocol.JobParameters;

import java.util.Random;

/**
 * {@link JobGenerator} implementation creating jobs with exponential
 * distribution of required CPU time.
 * 
 * @author marcinlos
 */
public class ExpJobGenerator implements JobGenerator {

    private Random random = new Random();
    private int avgTime;
    
    /**
     * Creates exponential distribution of job costs with average cost
     * {@code avgTime}
     * 
     * @param avgTime Average job time
     */
    public ExpJobGenerator(int avgTime) {
        this.avgTime = avgTime;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JobParameters nextJob() {
        double u = random.nextDouble();
        int cputime = 1 + (int) (- Math.log(u) * avgTime);
        JobParameters params = new JobParameters(cputime);
        return params;
    }

}
