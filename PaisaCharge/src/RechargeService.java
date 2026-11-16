import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Units 2 and 4: implements Rechargeable and does all database work with JDBC. */
public class RechargeService implements Rechargeable {

    static void validate(String mobile) throws InvalidNumberException {
        if (mobile == null || !mobile.matches("[6-9]\\d{9}"))
            throw new InvalidNumberException("Enter a 10-digit mobile number starting with 6, 7, 8 or 9.");
    }

    public void addSubscriber(String name, String mobile, String type) throws InvalidNumberException, SQLException {
        if (name.isBlank()) throw new IllegalArgumentException("Enter the subscriber's name.");
        validate(mobile);
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO subscribers(name, mobile, type) VALUES (?,?,?)")) {
            ps.setString(1, name); ps.setString(2, mobile); ps.setString(3, type);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new InvalidNumberException("This number is already registered.");
        }
    }

    public void deleteSubscriber(int id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM subscribers WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    public List<Subscriber> subscribers() throws SQLException {
        List<Subscriber> list = new ArrayList<>();
        try (Connection c = Db.get(); Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT id,name,mobile,type,expiry FROM subscribers ORDER BY id")) {
            while (r.next())
                list.add(new Subscriber(r.getInt(1), r.getString(2), r.getString(3), r.getString(4), r.getTimestamp(5)));
        }
        return list;
    }

    public List<Plan> plans() throws SQLException {
        List<Plan> list = new ArrayList<>();
        try (Connection c = Db.get(); Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT id,name,price,days,data,type FROM plans ORDER BY type, price")) {
            while (r.next())
                list.add(Plan.create(r.getInt(1), r.getString(2), r.getDouble(3), r.getInt(4), r.getString(5), r.getString(6)));
        }
        return list;
    }

    private Plan findPlan(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id,name,price,days,data,type FROM plans WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? Plan.create(r.getInt(1), r.getString(2), r.getDouble(3), r.getInt(4), r.getString(5), r.getString(6)) : null;
            }
        }
    }

    @Override
    public Recharge recharge(String mobile, int planId) throws InvalidNumberException, SQLException {
        validate(mobile);
        try (Connection c = Db.get()) {
            int sid = 0; String name = null, stype = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT id,name,type FROM subscribers WHERE mobile=?")) {
                ps.setString(1, mobile);
                try (ResultSet r = ps.executeQuery()) {
                    if (!r.next()) throw new InvalidNumberException("This number is not registered. Add it under Subscribers first.");
                    sid = r.getInt(1); name = r.getString(2); stype = r.getString(3);
                }
            }
            Plan p = findPlan(c, planId);
            if (p == null) throw new IllegalArgumentException("Select a plan.");
            if (!p.getType().equals(stype))
                throw new IllegalArgumentException("This is a " + stype + " subscriber. Pick a " + stype + " plan.");

            // Unit 4: CallableStatement calls the stored procedure recharge_plan
            try (CallableStatement cs = c.prepareCall("{call recharge_plan(?,?,?,?,?,?)}")) {
                cs.setInt(1, sid); cs.setInt(2, planId);
                cs.setDouble(3, p.cost()); cs.setDouble(4, p.base()); cs.setDouble(5, p.gst());
                cs.registerOutParameter(6, Types.TIMESTAMP);
                cs.execute();
                Timestamp expiry = cs.getTimestamp(6);
                int rid = 0;
                try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT LAST_INSERT_ID()")) {
                    if (r.next()) rid = r.getInt(1);
                }
                return new Recharge(rid, name, mobile, p.getName(), stype, p.cost(), p.base(), p.gst(),
                                    new Timestamp(System.currentTimeMillis()), expiry);
            }
        }
    }

    public List<Recharge> history() throws SQLException {
        List<Recharge> list = new ArrayList<>();
        String q = "SELECT r.id,s.name,s.mobile,p.name,p.type,r.amount,r.base,r.gst,r.recharged_at,r.expiry " +
                   "FROM recharges r JOIN subscribers s ON s.id=r.subscriber_id JOIN plans p ON p.id=r.plan_id ORDER BY r.id DESC";
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet r = st.executeQuery(q)) {
            while (r.next())
                list.add(new Recharge(r.getInt(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5),
                        r.getDouble(6), r.getDouble(7), r.getDouble(8), r.getTimestamp(9), r.getTimestamp(10)));
        }
        return list;
    }
}
