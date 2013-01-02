package hesp.agents;

/**
 * @author marcinlos
 *
 * Listener interface for monitoring progress & completion events for 
 * queued jobs.
 */
public interface JobProgressListener {
    
    /**
     * Invoked when the job's execution begins.
     * 
     * @param job Description of job whose execution has just started
     */
    void started(JobProgress job);
    
    void update(JobProgress job);
    
    void completed(JobProgress job, String details);
    
}