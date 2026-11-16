/** Unit 2: prepaid price already includes GST. */
public class PrepaidPlan extends Plan {
    public PrepaidPlan(int id, String name, double price, int days, String data) {
        super(id, name, price, days, data, "Prepaid");
    }
    @Override public String describe() { return super.describe() + " | GST included"; }
}
