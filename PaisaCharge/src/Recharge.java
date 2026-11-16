import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/** Unit 1: one completed recharge, also knows how to print its bill. */
public class Recharge {
    private final int id;
    private final String name, mobile, planName, type;
    private final double amount, base, gst;
    private final Timestamp date, expiry;

    public Recharge(int id, String name, String mobile, String planName, String type,
                    double amount, double base, double gst, Timestamp date, Timestamp expiry) {
        this.id = id; this.name = name; this.mobile = mobile; this.planName = planName; this.type = type;
        this.amount = amount; this.base = base; this.gst = gst; this.date = date; this.expiry = expiry;
    }

    public static String fmt(Timestamp t) {
        return t == null ? "-" : new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(t);
    }
    public int getId() { return id; }
    public String getMobile() { return mobile; }
    public String getPlanName() { return planName; }
    public double getAmount() { return amount; }
    public Timestamp getDate() { return date; }

    public String bill() {
        return String.format(
            "\n   PAISACHARGE INVOICE\n   Bill no. PC-%05d\n   ----------------------------------------\n" +
            "   Subscriber  : %s\n   Mobile      : %s\n   Plan        : %s (%s)\n   Date        : %s\n" +
            "   Valid till  : %s\n   ----------------------------------------\n" +
            "   Plan amount : Rs. %9.2f\n   GST (18%%)   : Rs. %9.2f\n   TOTAL       : Rs. %9.2f\n",
            id, name, mobile, planName, type, fmt(date), fmt(expiry), base, gst, amount);
    }
}
