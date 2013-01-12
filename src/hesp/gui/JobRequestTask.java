package hesp.gui;

import hesp.protocol.Job;
import hesp.protocol.JobParameters;
import hesp.traffic.JobGenerator;
import hesp.traffic.TimeDistribution;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * Class responsible for generating job requests. This process is described
 * by job parameter generator and time distribution.
 * 
 * @author marcinlos
 */
public class JobRequestTask {
    
    /** 
     * Constant to be used as number of repeats in {@link #JobRequestTask(int, 
     * JobGenerator, TimeDistribution, JobRequestListener)} to denote lack
     * of upper limit of number of repeats.  
     */
    public static final int INDEFINITELY = -1;
    
    private int repeats = INDEFINITELY;
    private JobGenerator jobGen;
    private TimeDistribution time;
    private Timer timer;
    private JobRequestListener listener;
    
    public JobRequestTask(int repeats, JobGenerator jobs, 
            TimeDistribution time, JobRequestListener listener) {
        this.repeats = repeats;
        this.jobGen = jobs;
        this.time = time;
        this.listener = listener;
        double delay = time.nextDelay();
        this.timer = new Timer((int) (1000 * delay), action);
    }
    
    /**
     * Begins generating requests.
     */
    public void start() {
        timer.start();
    }
    
    /**
     * Ends generating requests.
     */
    public void stop() {
        timer.stop();
    }
    
    private ActionListener action = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JobParameters params = jobGen.nextJob();
            long id = Job.generateId(this, 0);
            Job job = new Job(id, params);
            listener.requestIssued(job);
            if (repeats != INDEFINITELY) {
                -- repeats;
                if (repeats <= 0) {
                    timer.stop();
                }
            }
            // sec -> milisec
            int delay = (int) (1000 * time.nextDelay());
            timer.setDelay(delay);
        }
    };

}
