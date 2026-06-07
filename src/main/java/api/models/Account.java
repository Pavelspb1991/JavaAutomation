package api.models;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Account extends  BaseModel{
    private Long id;
    private String accountNumber;
    private Number balance;
    private List<Transaction> transactions;
}