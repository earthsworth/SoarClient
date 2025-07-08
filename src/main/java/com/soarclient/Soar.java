package com.soarclient;

import com.soarclient.animation.Delta;
import com.soarclient.event.EventBus;
import com.soarclient.event.server.PacketHandler;
import com.soarclient.libraries.browser.JCefBrowser;
import com.soarclient.management.color.ColorManager;
import com.soarclient.management.config.ConfigManager;
import com.soarclient.management.config.ServiceOverrides;
import com.soarclient.management.hypixel.HypixelManager;
import com.soarclient.management.mod.ModManager;
import com.soarclient.management.music.MusicManager;
import com.soarclient.management.profile.ProfileManager;
import com.soarclient.management.user.UserManager;
import com.soarclient.management.websocket.WebSocketManager;
import com.soarclient.skia.font.Fonts;
import com.soarclient.utils.file.FileLocation;
import com.soarclient.utils.language.I18n;
import com.soarclient.utils.language.Language;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

@Getter
public class Soar {

	@Getter
    private final static Soar instance = new Soar();

	private final String name = "Soar";
	private final String version = "8.0";

	private long launchTime;

	private ModManager modManager;
	private ColorManager colorManager;
	private MusicManager musicManager;
	private ConfigManager configManager;
	private ProfileManager profileManager;
	private WebSocketManager webSocketManager;
	private UserManager userManager;
	private HypixelManager hypixelManager;

	public void start() {
		ServiceOverrides serviceOverrides = this.parseServiceOverrides();
		URI rpcAddress = serviceOverrides.getRpcAddress();

		JCefBrowser.download();
		Fonts.loadAll();
		FileLocation.init();
		I18n.setLanguage(Language.ENGLISH);

		launchTime = System.currentTimeMillis();

		modManager = new ModManager();
		modManager.init();
		colorManager = new ColorManager();
		musicManager = new MusicManager();
		configManager = new ConfigManager();
		profileManager = new ProfileManager();
		webSocketManager = new WebSocketManager(rpcAddress);
		userManager = new UserManager();
		hypixelManager = new HypixelManager();

		EventBus.getInstance().register(new SoarHandler());
		EventBus.getInstance().register(new PacketHandler());
		EventBus.getInstance().register(new Delta());
	}

	private @NotNull ServiceOverrides parseServiceOverrides() {
		ServiceOverrides serviceOverrides = new ServiceOverrides();

		// parse service override from properties
		serviceOverrides.setRpcAddress(URI.create(System.getProperty("serviceOverrideRpc", "wss://api.lunarclient.top/ws/soar")));
		return serviceOverrides;
	}
}
