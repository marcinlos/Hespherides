package hesp.policy;

import hesp.protocol.Job;

public class FullUsage implements UsagePolicy {

    @Override
    public Result canUse(Job job) {
        return Result.YES;
    }

    @Override
    public Result use(Job job) {
        return Result.YES;
    }

}
