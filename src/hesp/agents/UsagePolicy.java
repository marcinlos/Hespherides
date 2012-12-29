package hesp.agents;

import jade.util.leap.Serializable;
import hesp.protocol.Job;

public interface UsagePolicy extends Serializable {
    
    boolean canUse(Job job);
    
    boolean use(Job job);
    
}
