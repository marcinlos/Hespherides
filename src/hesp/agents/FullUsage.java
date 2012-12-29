package hesp.agents;

import hesp.protocol.Job;

public class FullUsage implements UsagePolicy {

    @Override
    public boolean canUse(Job job) {
        return true;
    }

    @Override
    public boolean use(Job job) {
        return true;
    }

}
