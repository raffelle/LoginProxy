package monplugin.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
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

    private final Map<String, Boolean> loggedIP = new HashMap<>();

    public static final MinecraftChannelIdentifier LOGIN_CHANNEL =
            MinecraftChannelIdentifier.from("login:success");

    @Inject
    public Main(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;

        server.getChannelRegistrar().register(LOGIN_CHANNEL);
    }

    public boolean isBypassLogin(Player player) {
        String key = player.getUsername() + player.getRemoteAddress().getAddress().getHostAddress();
        return loggedIP.getOrDefault(key, false);
    }

    public void setLoggedIn(Player player) {
        String key = player.getUsername() + player.getRemoteAddress().getAddress().getHostAddress();
        loggedIP.put(key, true);
        logger.info("✅ Joueur authentifié sur le proxy : " + player.getUsername());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        String key = player.getUsername() + player.getRemoteAddress().getAddress().getHostAddress();
        loggedIP.remove(key);
    }

    // ✅ BLOQUE UNIQUEMENT TANT QUE PAS AUTH
    @Subscribe
    public void onCommand(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) return;

        if (isBypassLogin(player)) return;

        String msg = event.getCommand().toLowerCase();
        if (!msg.equals("login") && !msg.startsWith("login ")) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            player.sendMessage(Component.text("Tu dois d'abord te connecter sur le lobby avec /login"));
        }
    }

    // ✅ ✅ ✅ RÉCEPTION CORRECTE DU MESSAGE DEPUIS LE LOBBY
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(LOGIN_CHANNEL)) return;

        // ✅ SOURCE = LE SERVEUR (Lobby), PAS LE JOUEUR
        if (!(event.getSource() instanceof ServerConnection serverConnection)) return;

        Player player = serverConnection.getPlayer();

        setLoggedIn(player);

        player.sendMessage(Component.text("✅ Authentification proxy validée !"));
    }
}

