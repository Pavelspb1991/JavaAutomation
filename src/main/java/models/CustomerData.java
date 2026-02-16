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


public class UpdateCustomerProfileResponse extends BaseModel{
    private long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<String> accounts;
    private String message;

}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerData extends BaseModel {
    private String message;
    private UpdateCustomerProfileResponse customer;  // ← вложенный объект!
}
