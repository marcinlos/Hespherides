package hesp.agents;

/**
 * @author marcinlos
 *
 * Listener interface for monitoring progress & completion events for 
 * queued jobs.
 */
public interface JobProgressListener {
    
    void update(JobProgress job);
    
    void completed(JobProgress job, String details);
    
}