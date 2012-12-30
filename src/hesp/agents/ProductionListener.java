package hesp.agents;

import java.util.EventListener;

import hesp.agents.Computation.JobStatus;

/**
 * @author marcinlos
 * 
 * Interface for receiving notifications about production agent events.
 */
public interface ProductionListener extends EventListener {
    
    void jobAdded(JobStatus job);
    
    void jobFinished(JobStatus job);
    
    void jobUpdate(JobStatus job);
    
    

}
