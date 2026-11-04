package api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@JsonIgnoreProperties(ignoreUnknown = true)
public class DepositMoneyResponse extends BaseModel {
    private long id;
    private String accountNumber;
    private Number balance;
    private List<Transaction> transactions;
    private Number depositAmount;
}