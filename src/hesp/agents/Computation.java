package hesp.agents;

import hesp.protocol.Job;
import hesp.protocol.JobParameters;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Behaviour simulating computation of the underlying resource. It is 
 * realized as an infinite loop, where each iteration represents a fixed
 * timestep (time slice given to clients). 
 * 
 * @author marcinlos
 */
public class Computation extends TickerBehaviour {
    
    /** Time between invocations in milliseconds */
    private long timeSlice = 50;
    
    private double failPerSlice = 0.001;
    private Random failer = new Random();
    
    /** Maximum number of concurrently running jobs */
    private int processors;
    /** Job queue - fifo */
    private List<JobProgress> jobs = new ArrayList<>();
    
    private JobProgressListener listener;
    

    public Computation(Agent a, int processors, long timeSlice, 
            JobProgressListener listener) {
        super(a, timeSlice);
        this.timeSlice = timeSlice;
        this.processors = processors;
        this.listener = listener;
    }
    
    /**
     * Adds job to execution queue.
     * 
     * @param job description of job to execute
     */
    public void queueJob(Job job) {
        long id = job.getId();
        JobParameters params = job.getParameters();
        JobProgress js = new JobProgress(id, params.getCputime());
        jobs.add(js);
    }
    
    /**
     * @return total number of job slots (both used and unused)
     */
    public int getProcessors() {
        return processors;
    }
    
    /**
     * Changes the total number of job slots available for this resource
     * 
     * @param processors number of job slots
     */
    public void setProcessors(int processors) {
        this.processors = processors;
    }
    
    /**
     * @return number of milliseconds between consecutive job updates
     */
    public long getTimeSlice() {
        return timeSlice;
    }
    
    /**
     * Sets the time between consecutive job updates
     * 
     * @param timeSlice number of milliseconds
     */
    public void setTimeSlice(long timeSlice) {
        this.timeSlice = timeSlice;

        // Only way to change TickerBehaviour's period
        reset(timeSlice);
    }
    
    /**
     * @return probability that the job will fail during one time slice 
     */
    public double getFailChance() {
        return failPerSlice;
    }
    
    /**
     * Sets the chance that job will fail in some fixed time slice
     * 
     * @param chance probability of failure
     */
    public void setFailChance(double chance) {
        this.failPerSlice = chance;
    }
    
    /**
     * @return number of currently running jobs
     */
    public int workload() {
        return Math.min(jobs.size(), processors);
    }
    
    /**
     * @return number of free job slots (processors)
     */
    public int freeProcessors() {
        return processors - workload();
    }
    
    /**
     * @return number of queued jobs (including currently executed)
     */
    public int queuedJobs() {
        return jobs.size();
    }
    

    @Override
    protected void onTick() {
        Iterator<JobProgress> it = jobs.iterator();
        int count = 0;
        while (it.hasNext() && count++ < processors) {
            JobProgress job = it.next();
            updateJob(job);
            // check for random failure
            boolean failure = failer.nextDouble() < failPerSlice;
            if (failure) {
                job.fail();
                it.remove();
                if (listener != null) {
                    listener.completed(job, "Critical hardware failure");
                } 
            }
            // remove & inform listener
            else if (job.hasSucceeded()) {
                it.remove();
                if (listener != null) {
                    listener.completed(job, null);
                } 
            }
            else if (listener != null) {
                listener.update(job);
            }
        }
    }
    
    private void updateJob(JobProgress job) {
        if (! job.isRunning()) {
            job.start();
            if (listener != null) {
                listener.started(job);
            }
        }
        job.update();
    }
    
}