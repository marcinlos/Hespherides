package hesp.protocol;

public class TransferResponse {
    
    public final long id;
    public final boolean success;
    public final String details;
    
    public static TransferResponse failure(long id, String details) {
        return new TransferResponse(id, false, details);
    }
    
    public static TransferResponse success(long id) {
        return new TransferResponse(id, true, null);
    }
    
    private TransferResponse(long id, boolean success, String details) {
        this.id = id;
        this.success = success;
        this.details = details;
    }
}
