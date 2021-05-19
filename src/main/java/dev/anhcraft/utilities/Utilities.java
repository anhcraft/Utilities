package dev.anhcraft.utilities;

import dev.anhcraft.utilities.gui.MenuGUI;
import dev.anhcraft.utilities.gui.UtilScreen;
import dev.anhcraft.utilities.utils.PlayerTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.File;

@Environment(EnvType.CLIENT)
public class Utilities implements ClientModInitializer {
    private static Utilities instance;
    private File modFolder;
    private PlayerTracker playerTracker;
    private KeyBinding openMenuKey;
    private KeyBinding trackPlayerKey;

    public static Utilities getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        modFolder = new File(MinecraftClient.getInstance().runDirectory, "utilities");
        //noinspection ResultOfMethodCallIgnored
        modFolder.mkdirs();

        playerTracker = new PlayerTracker();
        openMenuKey = new KeyBinding("key.utilities.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.misc");
        trackPlayerKey = new KeyBinding("key.utilities.track_player", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.categories.misc");
        KeyBindingHelper.registerKeyBinding(openMenuKey);
        KeyBindingHelper.registerKeyBinding(trackPlayerKey);

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if(minecraftClient.player == null) return;
            while (openMenuKey.wasPressed()) {
                minecraftClient.openScreen(new UtilScreen(new MenuGUI()));
            }
            while (trackPlayerKey.wasPressed() && minecraftClient.currentScreen == null) {
                Utilities.getInstance().getPlayerTracker().nextTarget(minecraftClient.player);
            }
        });
    }

    public File getModFolder() {
        return modFolder;
    }

    public PlayerTracker getPlayerTracker() {
        return playerTracker;
    }
}
