package models;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class TransferMoneyResponse extends BaseModel{
    private int senderAccountId;
    private String message;
    private Number amount;
    private int receiverAccountId;
}