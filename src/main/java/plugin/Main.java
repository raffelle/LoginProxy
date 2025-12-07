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

@Plugin(id = "loginproxy", name = "LoginProxy", version = "1.0.3")
public class Main {

    private final Logger logger;
    private final ProxyServer server;

    // ✅ Stockage par username+IP pour que la session soit invalide si l'IP change
    private final Map<String, Boolean> loggedSessions = new HashMap<>();

    public static final MinecraftChannelIdentifier LOGIN_CHANNEL =
            MinecraftChannelIdentifier.from("login:success");

    @Inject
    public Main(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        server.getChannelRegistrar().register(LOGIN_CHANNEL);
    }

    private String getKey(Player player) {
        return player.getUsername() + "@" + player.getRemoteAddress().getAddress().getHostAddress();
    }

    public boolean isBypassLogin(Player player) {
        return loggedSessions.getOrDefault(getKey(player), false);
    }

    public void setLoggedIn(Player player) {
        loggedSessions.put(getKey(player), true);
        logger.info("Joueur authentifié sur le proxy : " + player.getUsername() + " | IP: " + player.getRemoteAddress().getAddress().getHostAddress());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        // ✅ On garde la session, donc on ne supprime rien ici
        Player player = event.getPlayer();
        logger.info("Joueur déconnecté du proxy : " + player.getUsername());
    }

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

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(LOGIN_CHANNEL)) return;
        if (!(event.getSource() instanceof ServerConnection serverConnection)) return;

        Player player = serverConnection.getPlayer();
        setLoggedIn(player);
        player.sendMessage(Component.text("✅ Authentification proxy validée !"));
    }
}
