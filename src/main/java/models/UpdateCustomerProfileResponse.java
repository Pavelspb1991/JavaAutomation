package models;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class UpdateCustomerProfileResponse extends BaseModel {
    private CustomerData customer;    // ← вложенный объект
    private String message;           // ← поле на верхнем уровне
}
