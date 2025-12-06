package monplugin.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Plugin(id = "loginproxy", name = "LoginProxy", version = "1.0")
public class Main {

    private final Logger logger;
    private final ProxyServer server;

    // Stocke joueurs déjà loggés : key = pseudo+IP
    private final Map<String, Boolean> loggedIP = new HashMap<>();

    @Inject
    public Main(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
    }

    // Vérifie si le joueur est déjà connecté avec pseudo + IP
    public boolean isBypassLogin(Player player) {
        String key = player.getUsername() + player.getRemoteAddress().getAddress().getHostAddress();
        return loggedIP.getOrDefault(key, false);
    }

    // Marque le joueur comme connecté
    public void setLoggedIn(Player player) {
        String key = player.getUsername() + player.getRemoteAddress().getAddress().getHostAddress();
        loggedIP.put(key, true);
    }

    // Reset lors de la déconnexion
    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        String key = player.getUsername() + player.getRemoteAddress().getAddress().getHostAddress();
        loggedIP.remove(key); // Le joueur devra se reconnecter avec le mdp
    }

    // Intercepte toutes les commandes proxy
    @Subscribe
    public void onCommand(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) return;

        if (isBypassLogin(player)) return; // déjà connecté, autorise tout

        String msg = event.getCommand().toLowerCase();
        if (!msg.equals("login") && !msg.startsWith("login ")) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            player.sendMessage(Component.text("Tu dois d'abord te connecter avec /login"));
        }
    }
}
