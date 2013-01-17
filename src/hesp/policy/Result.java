package hesp.policy;

public class Result {
    
    public final boolean result;
    public final String message;
    
    public static final Result YES = yes("ok");
    public static final Result NO = no("no");
    
    public Result(boolean result, String message) {
        this.result = result;
        this.message = message;
    }
    
    public static Result yes(String message) {
        return new Result(true, message);
    }
    
    public static Result no(String message) {
        return new Result(false, message);
    }
}