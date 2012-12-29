package hesp.policy;

public class Constraint {

    public enum Op {
        GT, LT
    }
    
    public Metric metric;
    public int value;
    public Op relation;
}
