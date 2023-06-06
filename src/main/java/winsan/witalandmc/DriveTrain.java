package winsan.witalandmc;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class DriveTrain extends JavaPlugin{

    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"TrainThrottle is now enabled.");
        getConfig().options().copyDefaults(true);
        saveConfig();
        TrainThrottle throttle = new TrainThrottle();
        Objects.requireNonNull(getCommand("traindrive")).setExecutor(throttle);
        getServer().getPluginManager().registerEvents(throttle, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, throttle::throttleloop, 0L, 1L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveConfig();
        getServer().getConsoleSender().sendMessage(ChatColor.RED+"Stupid TMGT6671. Plugin TrainThrottle is now disabled. Okay?");
    }
}
