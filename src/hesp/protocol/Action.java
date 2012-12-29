package hesp.protocol;

public enum Action {

    SET_OWNER(Category.CONTROL),
    SET_POLICY(Category.CONTROL),
    
    JOB_REQUEST(Category.JOB),
    JOB_SUBMITTED(Category.JOB),
    JOB_COMPLETED(Category.JOB),
    
    CREATE_ACCOUNT(Category.BANK),
    CREATE_ACCOUNT_ACK(Category.BANK),
    TRANSFER(Category.BANK),
    TRANSFER_ACK(Category.BANK),
    CHECK_ACC_STATE(Category.BANK),
    ACC_STATE(Category.BANK);
    
    
    public enum Category {
        CONTROL, JOB, BANK
    }
    
    private final Category category;
    
    Action(Category category) {
        this.category = category;
    }
    
    public Category category() {
        return category;
    }
    
}
