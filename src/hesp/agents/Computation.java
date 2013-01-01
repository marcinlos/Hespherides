package hesp.agents;

import hesp.protocol.Job;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @author marcinlos
 * 
 * Behaviour simulating computation of the underlying resource. It is 
 * realized as an infinite loop, where each iteration represents a fixed
 * timestep (time slice given to clients). 
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
        JobProgress js = new JobProgress(job.getId(), job.getCputime());
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

    @Override
    protected void onTick() {
        Iterator<JobProgress> it = jobs.iterator();
        int count = 0;
        while (it.hasNext() && count++ < processors) {
            JobProgress job = it.next();
            job.update();
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
    
}