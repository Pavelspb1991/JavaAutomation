package api.requests.steps;

import api.dao.AccountDao;
import api.dao.UserDao;
import api.database.Condition;
import api.database.DBRequest;

public class DataBaseSteps {

    public static UserDao getUserByUsername(String username) {
        return DBRequest.builder()
                .requestType(DBRequest.RequestType.SELECT)
                .table("customers")
                .where(Condition.equalTo("username", username))
                .extractAs(UserDao.class);
    }

    public static AccountDao getAccountByAccountNumber(String accountNumber) {
        return DBRequest.builder()
                .requestType(DBRequest.RequestType.SELECT)
                .table("accounts")
                .where(Condition.equalTo("account_number", accountNumber))
                .extractAs(AccountDao.class);
    }
}