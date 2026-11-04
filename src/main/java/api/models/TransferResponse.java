package api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferResponse extends BaseModel {
    private String status;
    private String message;
    private Number amount;
    private Long senderAccountId;
    private Long receiverAccountId;
    private Double fraudRiskScore;
    private String fraudReason;
    private Boolean requiresManualReview;
    private Boolean requiresVerification;
}