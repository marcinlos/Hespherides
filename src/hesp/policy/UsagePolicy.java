package hesp.policy;

import jade.util.leap.Serializable;
import hesp.protocol.Job;

public interface UsagePolicy extends Serializable {
    
    Result canUse(Job job);
    
    Result use(Job job);
    
}
