package hesp.protocol;

public class AccountResponse {
    
    public final boolean succeeded;
    public final String id;
    public final String details;
    
    public static AccountResponse failure(String details) {
        return new AccountResponse(false, null, details);
    }
    
    public static AccountResponse success(String id) {
        return new AccountResponse(true, id, null);
    }
    
    private AccountResponse(boolean success, String id, String details) {
        this.succeeded = success;
        this.id = id;
        this.details = details;
    }
    
}
