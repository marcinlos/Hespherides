package hesp.agents;

import java.util.EventListener;


/**
 * Interface for receiving notifications about production agent events.
 * 
 * @author marcinlos
 */
public interface ProductionListener extends EventListener {
    
    /**
     * Invoked after job has been added to the queue of jobs waiting for
     * execution.
     * 
     * @param job Description of the queued job
     */
    void jobQueued(JobProgress job);
    
    /**
     * Invoked when the job's execution begins.
     * 
     * @param job Description of job whose execution has just started
     */
    void jobStarted(JobProgress job);
    
    /**
     * Invoked when job's execution has came to an end, i.e. it has failed,
     * or has been successfully executed.
     * 
     * @param job Description of job whose execution has finished
     */
    void jobFinished(JobProgress job);
    
    /**
     * Invoked after having performed some work upon the job.
     * 
     * @param job Description of updated job
     */
    void jobUpdate(JobProgress job);
    
}
