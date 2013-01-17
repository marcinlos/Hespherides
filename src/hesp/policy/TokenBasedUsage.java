package hesp.policy;

import hesp.protocol.Job;

public class TokenBasedUsage implements UsagePolicy {

    private int tokens;
    
    public TokenBasedUsage(int tokens) {
        this.tokens = tokens;
    }
    
    private boolean conditionsMet() {
        return tokens > 0;
    }
    
    @Override
    public Result canUse(Job job) {
        if (conditionsMet()) {
            return Result.yes("ok");
        } else {
            return Result.no("Insufficient tokens");
        }
    }

    @Override
    public Result use(Job job) {
        Result res = canUse(job);
        if (res.result) {
            -- tokens;
        }
        return res;
    }

}
