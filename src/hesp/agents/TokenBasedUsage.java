package hesp.agents;

import hesp.protocol.Job;

public class TokenBasedUsage implements UsagePolicy {

    private int tokens;
    
    public TokenBasedUsage(int tokens) {
        this.tokens = tokens;
    }
    
    @Override
    public boolean canUse(Job job) {
        return tokens > 0;
    }

    @Override
    public boolean use(Job job) {
        if (canUse(job)) {
            -- tokens;
            return true;
        } else {
            return false;
        }
    }

}
