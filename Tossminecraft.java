package toss.tossminecraft;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class Tossminecraft extends JavaPlugin {

    private static final String API_BASE_URL = "https://toss.im/tosspay/api/";
    private String apiKey;
    private String apiSecret;
    private String targetAccount;
    private double targetAmount;
    private String targetCommand;

    @Override
    public void onEnable() {
        super.onEnable();

        loadConfig(); // config.yml 파일을 로드합니다.

        new BukkitRunnable() {
            @Override
            public void run() {
                checkTransfers();
            }
        }.runTaskTimer(this, 0L, 20L); // 1초마다 송금내역을 확인합니다.
    }

    private void loadConfig() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        apiKey = config.getString("api_key");
        apiSecret = config.getString("api_secret");
        targetAccount = config.getString("target_account");
        targetAmount = config.getDouble("target_amount");
        targetCommand = config.getString("target_command");
    }

    private void checkTransfers() {
        try {
            URL url = new URL(API_BASE_URL + "v1/payments?apiKey=" + apiKey + "&secret=" + apiSecret + "&status=COMPLETED");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            in.close();

            String responseBody = response.toString();
            if (responseBody.contains("\"accountNumber\":\"" + targetAccount + "\"")) {
                String[] payments = responseBody.split("\"paymentMethod\":\"TRANSFER\"");
                for (String payment : payments) {
                    if (payment.contains("\"amount\":" + targetAmount)) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), targetCommand);
                    }
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Toss 전송 확인에 실패했습니다.", e);
        }
    }
}
