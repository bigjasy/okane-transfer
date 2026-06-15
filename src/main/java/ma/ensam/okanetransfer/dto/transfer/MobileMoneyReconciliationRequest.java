package ma.ensam.okanetransfer.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ma.ensam.okanetransfer.enums.MobileMoneyOperator;

public class MobileMoneyReconciliationRequest {

    @NotNull(message = "L'opérateur est obligatoire")
    private MobileMoneyOperator operator;

    @NotBlank(message = "La date de réconciliation est obligatoire")
    private String date;

    public MobileMoneyOperator getOperator() {
        return operator;
    }

    public void setOperator(MobileMoneyOperator operator) {
        this.operator = operator;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
