package ma.ensam.okanetransfer.dto.agency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FeeGridRequestTest {

    @Test
    void shouldDeserializeMaxAmountFromApiContractField() throws Exception {
        String json = """
                {
                  "corridorId": 1,
                  "minAmount": 0,
                  "maxAmount": 1000,
                  "fixedFee": 20,
                  "percentageFee": 1.5
                }
                """;

        FeeGridRequest request = new ObjectMapper().readValue(json, FeeGridRequest.class);

        assertNotNull(request.getMaxAmount());
        assertEquals(0, new BigDecimal("1000").compareTo(request.getMaxAmount()));
    }
}
