package dev.anhcraft.utilities.utils;

import net.minecraft.command.arguments.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class PlayerTracker {
    private final List<LivingEntity> possibleTargets = Collections.synchronizedList(new ArrayList<>());
    private final Object LOCK = new Object();
    private LivingEntity target;

    public LivingEntity getTarget() {
        return target;
    }

    public void setTarget(PlayerEntity player, LivingEntity target) {
        synchronized (LOCK) {
            if (target == null) {
                player.sendMessage(new LiteralText("No longer tracking ").formatted(Formatting.GOLD).append(this.target.getDisplayName()), false);
                this.target.setGlowing(false);
            } else {
                if (this.target != null) {
                    if (this.target.getUuid() == target.getUuid()) {
                        return;
                    }
                    this.target.setGlowing(false);
                }
                target.setGlowing(true);
                player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());
                player.sendMessage(new LiteralText("Now tracking ").formatted(Formatting.GREEN).append(target.getDisplayName()), false);
            }
            this.target = target;
        }
    }

    public void nextTarget(PlayerEntity player) {
        synchronized (LOCK) {
            if (possibleTargets.isEmpty()) {
                if (target != null) {
                    setTarget(player, null);
                } else {
                    player.sendMessage(new LiteralText("Nothing to track!").formatted(Formatting.RED), false);
                }
            } else {
                boolean chooseNext = (target == null);
                for (LivingEntity entity : possibleTargets) {
                    if(player.isSneaking() && entity.getType() != EntityType.PLAYER) continue;
                    if (chooseNext) {
                        setTarget(player, entity);
                        return;
                    } else if (entity.getUuid().equals(target.getUuid())) {
                        chooseNext = true;
                    }
                }
                if (chooseNext) {
                    setTarget(player, possibleTargets.get(0));
                } else {
                    player.sendMessage(new LiteralText("Nothing to track!").formatted(Formatting.RED), false);
                }
            }
        }
    }

    private void show(PlayerEntity player){
        if(!target.isAlive()) {
            setTarget(player, null);
            return;
        }
        LiteralText txt = new LiteralText("");
        txt.append(new LiteralText("Target: ").formatted(Formatting.GOLD));
        txt.append(target.getDisplayName());
        txt.append(" | ");
        txt.append(new LiteralText("Distance: ").formatted(Formatting.GREEN));
        txt.append(String.format("%.2f", player.distanceTo(target)));
        txt.append(" | ");
        txt.append(new LiteralText("Health: ").formatted(Formatting.RED));
        txt.append(String.format("%.2f", target.getHealth()));
        txt.append("/");
        txt.append(String.format("%.2f", target.getMaxHealth()));
        player.sendMessage(txt, true);
    }

    public void onTick(PlayerEntity player){
        if (ThreadLocalRandom.current().nextInt(0, 50) == 0) {
            possibleTargets.removeIf(t -> {
                boolean b = !t.isAlive() || (player.squaredDistanceTo(t) >= 64 * 64); // doubles the bounding box radius 32x2 = 64
                if(b && target != null && t.getUuid().equals(target.getUuid())) {
                    setTarget(player, null);
                }
                return b;
            });
            player.getEntityWorld().getEntities(player, player.getBoundingBox().expand(32), entity -> entity instanceof Monster || entity instanceof PlayerEntity)
                    .stream()
                    .map(e -> (LivingEntity) e)
                    .filter(((Predicate<LivingEntity>) possibleTargets::contains).negate())
                    .forEach(possibleTargets::add);
            possibleTargets.sort(Comparator.comparingDouble(player::distanceTo));
        }
        if (target != null && ThreadLocalRandom.current().nextInt(0, 5) == 0) {
            show(player);
        }
    }
}
