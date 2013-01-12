package hesp.traffic;

/**
 * Interface of job generator provider, to abstract an act of creating a job
 * generator from a set of parameters. Example use is implementing it by
 * a GUI widget.
 * 
 * It is not, strictly speaking, a factory, as it allows returning cached
 * job generator objects. Care should be taken to avoid multithreading issues.
 * 
 * @author marcinlos
 * @see JobGenerator
 * @see TimeDistributionProvider
 */
public interface JobGeneratorProvider {

    /**
     * @return a job generator, not necessarily newly created
     */
    JobGenerator getGenerator();
    
}
