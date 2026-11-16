import java.sql.Timestamp;

/** Unit 1: subscriber record. */
public class Subscriber {
    private final int id;
    private final String name, mobile, type;
    private final Timestamp expiry;

    public Subscriber(int id, String name, String mobile, String type, Timestamp expiry) {
        this.id = id; this.name = name; this.mobile = mobile; this.type = type; this.expiry = expiry;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getMobile() { return mobile; }
    public String getType() { return type; }
    public Timestamp getExpiry() { return expiry; }

    public String status() {
        if (expiry == null) return "New";
        return expiry.getTime() > System.currentTimeMillis() ? "Active" : "Expired";
    }
}
