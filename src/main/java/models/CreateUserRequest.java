package models;
import generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateUserRequest extends BaseModel {
    @GeneratingRule(regex = "[a-zA-Z0-9]{5,15}")
    private String username;

    @GeneratingRule(regex = "[A-Z]{3}[a-z]{5}[0-9]{3}%")
    private String password;

    private UserRole role;
}