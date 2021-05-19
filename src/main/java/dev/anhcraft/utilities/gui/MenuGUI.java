package dev.anhcraft.utilities.gui;

import com.google.common.io.Files;
import dev.anhcraft.utilities.Utilities;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class MenuGUI extends LightweightGuiDescription {
    public MenuGUI(){
        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(100, 100);

        WButton scbBtn = new WButton(new LiteralText("Download Scoreboard"));
        scbBtn.setOnClick(() -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if(player == null) return;
            Scoreboard scoreboard = player.getScoreboard();
            if(scoreboard == null) return;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[OBJECTIVES]").append('\n');
            for(ScoreboardObjective objective : scoreboard.getObjectives()){
                stringBuilder.append("Id: ").append(objective.getName()).append('\n');
                stringBuilder.append("Name: ").append(Text.Serializer.toJson(objective.getDisplayName())).append('\n');
                stringBuilder.append("Render Type: ").append(objective.getRenderType().getName()).append('\n');
                stringBuilder.append("Criterion: ").append('\n');
                stringBuilder.append("- Name: ").append(objective.getCriterion().getName()).append('\n');
                stringBuilder.append("- Render Type: ").append(objective.getCriterion().getCriterionType()).append('\n');
                stringBuilder.append('\n');
            }
            stringBuilder.append("[TEAMS]").append('\n');
            for(Team team : scoreboard.getTeams()){
                stringBuilder.append("Id: ").append(team.getName()).append('\n');
                stringBuilder.append("Prefix: ").append(Text.Serializer.toJson(team.getPrefix())).append('\n');
                stringBuilder.append("Name: ").append(Text.Serializer.toJson(team.getDisplayName())).append('\n');
                stringBuilder.append("Suffix: ").append(Text.Serializer.toJson(team.getSuffix())).append('\n');
                stringBuilder.append("Color: ").append(team.getColor()).append('\n');
                stringBuilder.append("Players: ").append(String.join(", ", team.getPlayerList())).append('\n');
                stringBuilder.append("Friendly Fire: ").append(team.isFriendlyFireAllowed()).append('\n');
                stringBuilder.append("Friendly Invisible: ").append(team.shouldShowFriendlyInvisibles()).append('\n');
                stringBuilder.append("Name Tag Visibility: ").append(team.getNameTagVisibilityRule().name).append('\n');
                stringBuilder.append("Death Message Visibility: ").append(team.getDeathMessageVisibilityRule().name).append('\n');
                stringBuilder.append("Collision Rule: ").append(team.getCollisionRule().name).append('\n');
                stringBuilder.append('\n');
            }
            File dir = new File(Utilities.getInstance().getModFolder(), "scoreboards");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            File file = new File(dir, System.currentTimeMillis()+".txt");
            try {
                if(file.createNewFile()) {
                    //noinspection UnstableApiUsage
                    Files.write(stringBuilder.toString(), file, StandardCharsets.UTF_8);
                    LiteralText text = new LiteralText("");
                    text.append("Scoreboard has been saved to ");
                    LiteralText filePathText = new LiteralText(file.getName());
                    filePathText.setStyle(Style.EMPTY
                            .withFormatting(Formatting.UNDERLINE, Formatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()))
                    );
                    text.append(filePathText);
                    player.sendMessage(text, false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        root.add(scbBtn, 0, 0, 5, 1);

        WButton bbBtn = new WButton(new LiteralText("Download Boss Bar"));
        bbBtn.setOnClick(() -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if(player == null) return;
            BossBarHud bossbar = MinecraftClient.getInstance().inGameHud.getBossBarHud();
            StringBuilder stringBuilder = new StringBuilder();
            try {
                Field f = BossBarHud.class.getDeclaredField("bossBars");
                f.setAccessible(true);
                //noinspection unchecked
                Map<UUID, ClientBossBar> map = (Map<UUID, ClientBossBar>) f.get(bossbar);
                for (ClientBossBar bar : map.values()) {
                    stringBuilder.append("Id: ").append(bar.getUuid().toString()).append('\n');
                    stringBuilder.append("Name: ").append(Text.Serializer.toJson(bar.getName())).append('\n');
                    stringBuilder.append("Progress: ").append(bar.getPercent()).append('\n');
                    stringBuilder.append("Color: ").append(bar.getColor().getName()).append('\n');
                    stringBuilder.append("Style: ").append(bar.getOverlay().getName()).append('\n');
                    stringBuilder.append("Darken Sky: ").append(bar.getDarkenSky()).append('\n');
                    stringBuilder.append("Dragon Music: ").append(bar.hasDragonMusic()).append('\n');
                    stringBuilder.append("Thicken Fog: ").append(bar.getThickenFog()).append('\n');
                    stringBuilder.append('\n');
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            File dir = new File(Utilities.getInstance().getModFolder(), "bossbars");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            File file = new File(dir, System.currentTimeMillis()+".txt");
            try {
                if(file.createNewFile()) {
                    //noinspection UnstableApiUsage
                    Files.write(stringBuilder.toString(), file, StandardCharsets.UTF_8);
                    LiteralText text = new LiteralText("");
                    text.append("Boss Bar has been saved to ");
                    LiteralText filePathText = new LiteralText(file.getName());
                    filePathText.setStyle(Style.EMPTY
                            .withFormatting(Formatting.UNDERLINE, Formatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()))
                    );
                    text.append(filePathText);
                    player.sendMessage(text, false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        root.add(bbBtn, 0, 2, 5, 1);

        root.validate(this);
    }
}
