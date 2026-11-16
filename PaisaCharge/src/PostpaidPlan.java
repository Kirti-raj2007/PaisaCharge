/** Unit 2: postpaid adds 18% GST on top of the listed price (overrides cost, base, gst). */
public class PostpaidPlan extends Plan {
    public PostpaidPlan(int id, String name, double price, int days, String data) {
        super(id, name, price, days, data, "Postpaid");
    }
    @Override public double cost() { return Math.round(price * 1.18 * 100) / 100.0; }
    @Override public double base() { return price; }
    @Override public double gst()  { return Math.round(price * 0.18 * 100) / 100.0; }
    @Override public String describe() { return super.describe() + " | +18% GST"; }
}
