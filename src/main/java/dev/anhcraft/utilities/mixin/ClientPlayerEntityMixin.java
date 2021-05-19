package dev.anhcraft.utilities.mixin;

import com.mojang.authlib.GameProfile;
import dev.anhcraft.utilities.Utilities;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    @Shadow
    public abstract void sendMessage(Text message, boolean actionBar);

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(
            at = @At(
                    value = "JUMP",
                    shift = At.Shift.BEFORE,
                    ordinal = 1
            ),
            method = "tick()V"
    )
    public void onTick(CallbackInfo ci){/*
        if (ThreadLocalRandom.current().nextInt(0, 200) == 0) {
            getEntityWorld().getEntities(this, getBoundingBox().expand(32), entity -> entity instanceof PlayerEntity).stream().map(e -> e.getName().copy().append(" is nearby! (Distance: "+String.format("%.2f", e.distanceTo(this))+")")).forEach(m -> sendMessage(m, false));
        }*/
        Utilities.getInstance().getPlayerTracker().onTick(this);
    }
}
