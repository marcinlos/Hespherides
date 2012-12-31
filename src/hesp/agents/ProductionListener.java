package hesp.agents;

import java.util.EventListener;


/**
 * @author marcinlos
 * 
 * Interface for receiving notifications about production agent events.
 */
public interface ProductionListener extends EventListener {
    
    void jobAdded(JobProgress job);
    
    void jobFinished(JobProgress job);
    
    void jobUpdate(JobProgress job);
    
}
