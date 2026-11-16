import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/** Unit 3: background thread that checks plan expiry every 5 seconds. */
public class ExpiryChecker extends Thread {
    private final RechargeService svc;
    private final Consumer<String> statusOut;
    private final Runnable onNewExpiry;
    private final Set<String> seen = new HashSet<>();

    public ExpiryChecker(RechargeService svc, Consumer<String> statusOut, Runnable onNewExpiry) {
        this.svc = svc; this.statusOut = statusOut; this.onNewExpiry = onNewExpiry;
        setDaemon(true);
    }

    @Override public void run() {
        while (!isInterrupted()) {
            String text;
            boolean fresh = false;
            try {
                int active = 0, expired = 0;
                for (Subscriber s : svc.subscribers()) {
                    String st = s.status();
                    if (st.equals("Active")) active++;
                    if (st.equals("Expired")) { expired++; if (seen.add(s.getId() + ":" + s.getExpiry())) fresh = true; }
                }
                text = " Expiry watcher: " + active + " active, " + expired + " expired. Last check "
                       + new SimpleDateFormat("HH:mm:ss").format(new Date());
            } catch (SQLException e) {
                text = " Expiry watcher: database unavailable";
            }
            final String t = text; final boolean f = fresh;
            SwingUtilities.invokeLater(() -> { statusOut.accept(t); if (f) onNewExpiry.run(); });
            try { Thread.sleep(5000); } catch (InterruptedException e) { return; }
        }
    }
}
