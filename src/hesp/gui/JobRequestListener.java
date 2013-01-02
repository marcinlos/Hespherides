package hesp.gui;

import hesp.protocol.Job;

/**
 * Interface for receiving notifications about job requests from {@link
 * JobRequestTask}.
 * 
 * @author marcinlos
 * 
 * @see JobRequestTask
 */
public interface JobRequestListener {
    
    void requestIssued(Job job);
    
}
