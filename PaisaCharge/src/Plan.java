/** Unit 1: base class for plans (constructors, Math). Subclasses override cost(), base(), gst(), describe(). */
public class Plan {
    protected final int id, days;
    protected final String name, data, type;
    protected final double price;

    public Plan(int id, String name, double price, int days, String data, String type) {
        this.id = id; this.name = name; this.price = price;
        this.days = days; this.data = data; this.type = type;
    }

    public static Plan create(int id, String name, double price, int days, String data, String type) {
        return "Postpaid".equals(type) ? new PostpaidPlan(id, name, price, days, data)
                                       : new PrepaidPlan(id, name, price, days, data);
    }

    public double cost() { return price; }                                   // amount charged
    public double base() { return Math.round(price / 1.18 * 100) / 100.0; }  // amount before GST
    public double gst()  { return Math.round((cost() - base()) * 100) / 100.0; }
    public String describe() { return data + " | " + days + " days"; }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getDays() { return days; }

    @Override public String toString() { return name + " (" + type + ") - Rs." + String.format("%.2f", cost()); }
}
