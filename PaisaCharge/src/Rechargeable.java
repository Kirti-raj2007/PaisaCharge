import java.sql.SQLException;

/** Unit 2: interface implemented by RechargeService. */
public interface Rechargeable {
    Recharge recharge(String mobile, int planId) throws InvalidNumberException, SQLException;
}
