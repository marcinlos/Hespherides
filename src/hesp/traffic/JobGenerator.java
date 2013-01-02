package hesp.traffic;

import hesp.protocol.JobParameters;

/**
 * Interface of general job description generator. It's primary purpose is to
 * serve as an interface of random job generators with various distributions of 
 * parameters. 
 * 
 * @author marcinlos
 */
public interface JobGenerator {

    /**
     * @return new job description
     */
    JobParameters nextJob();
    
}
