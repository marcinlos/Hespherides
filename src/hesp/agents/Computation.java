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
    public static final long TIME_SLICE = 50;
    
    private double failPerSlice = 0.001;
    private Random failer = new Random();
    
    /**
     * Structure containing job-in-progress information. 
     */
    public static class JobStatus {
        private long id;
        private int required;
        private int done = 0;
        
        public JobStatus(long id, int required) {
            this.id = id;
            this.required = required;
        }
        
        public long getId() {
            return id;
        }
        
        public int getRequired() {
            return required;
        }
        
        public int getDone() {
            return done;
        }
        
        public boolean success() {
            return done == required;
        }
    }
    
    /** Maximum number of concurrently running jobs */
    private int processors;
    /** Job queue - fifo */
    private List<JobStatus> jobs = new ArrayList<>();
    
    private Listener listener;
    

    public Computation(Agent a, int processors, Listener listener) {
        super(a, TIME_SLICE);
        this.processors = processors;
        this.listener = listener;
    }
    
    public void queueJob(Job job) {
        JobStatus js = new JobStatus(job.getId(), 2 * job.getCputime());
        jobs.add(js);
    }
    
    public int processors() {
        return processors;
    }
    
    public int workload() {
        return Math.min(jobs.size(), processors);
    }
    
    public int freeProcessors() {
        return processors - workload();
    }

    @Override
    protected void onTick() {
        Iterator<JobStatus> it = jobs.iterator();
        int count = 0;
        while (it.hasNext() && count++ < processors) {
            JobStatus job = it.next();
            // check for random failure
            boolean failure = failer.nextDouble() < failPerSlice;
            if (failure) {
                it.remove();
                if (listener != null) {
                    listener.completed(job, "Critical hardware failure");
                } 
            }
            // remove & inform listener
            else if (++ job.done == job.required) {
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
    
    /**
     * @author marcinlos
     *
     * Listener interface for monitoring progress & completion events for 
     * queued jobs.
     */
    public interface Listener {
        
        void update(JobStatus job);
        
        void completed(JobStatus job, String details);
        
    }
    
}