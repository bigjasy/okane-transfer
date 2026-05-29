package ma.ensam.okanetransfer.dto.finance;

import java.math.BigDecimal;

public class CashClosingRequest {
    private BigDecimal countedAmount;
    private String comment;

    // Getters and Setters
    public BigDecimal getCountedAmount() {
        return countedAmount;
    }
    public void setCountedAmount(BigDecimal countedAmount) {
        this.countedAmount = countedAmount;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    
}