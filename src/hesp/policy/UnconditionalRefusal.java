package hesp.policy;

import hesp.protocol.Job;

public class UnconditionalRefusal implements UsagePolicy {
    
    private String message;
    
    public UnconditionalRefusal(String message) {
        this.message = message;
    }

    @Override
    public Result canUse(Job job) {
        return Result.no(message);
    }

    @Override
    public Result use(Job job) {
        return Result.no(message);
    }

}
