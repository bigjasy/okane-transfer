package ma.ensam.okanetransfer.dto.finance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class CashClosingRequest {
    @NotNull(message = "Le montant compté physiquement est obligatoire")
    @PositiveOrZero(message = "Le montant compté ne peut pas être négatif")
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