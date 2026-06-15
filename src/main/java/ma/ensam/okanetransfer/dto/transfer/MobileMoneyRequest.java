package ma.ensam.okanetransfer.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import ma.ensam.okanetransfer.enums.MobileMoneyOperator;

public class MobileMoneyRequest {

    @NotBlank(message = "La référence du transfert est obligatoire")
    private String transferReference;

    @NotNull(message = "L'opérateur Mobile Money est obligatoire")
    private MobileMoneyOperator operator;

    @NotBlank(message = "Le numéro de wallet est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Numéro de wallet invalide")
    private String walletPhoneNumber;

    public String getTransferReference() {
        return transferReference;
    }

    public void setTransferReference(String transferReference) {
        this.transferReference = transferReference;
    }

    public MobileMoneyOperator getOperator() {
        return operator;
    }

    public void setOperator(MobileMoneyOperator operator) {
        this.operator = operator;
    }

    public String getWalletPhoneNumber() {
        return walletPhoneNumber;
    }

    public void setWalletPhoneNumber(String walletPhoneNumber) {
        this.walletPhoneNumber = walletPhoneNumber;
    }
}
