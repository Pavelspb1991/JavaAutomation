package models;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class CreateAccountResponse extends BaseModel{
    private int id;
    private String accountNumber;
    private String balance;
    private List<String> transactions;
}