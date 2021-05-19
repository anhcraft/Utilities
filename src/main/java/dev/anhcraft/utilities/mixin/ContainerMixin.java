package dev.anhcraft.utilities.mixin;

import com.google.common.io.Files;
import dev.anhcraft.utilities.Utilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Mixin(ScreenHandler.class)
public abstract class ContainerMixin {
    @Shadow
    public abstract Slot getSlot(int index);

    @Inject(
            at = @At("HEAD"),
            method = "onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;",
            cancellable = true
    )
    public void onClickSlot(int slotId, int clickData, SlotActionType actionType, PlayerEntity playerEntity, CallbackInfoReturnable<ItemStack> info) {
        if(clickData == 1 && actionType == SlotActionType.PICKUP && slotId >= 0){
            ItemStack itemStack = getSlot(slotId).getStack();
            if (itemStack != null && !itemStack.isEmpty() && itemStack.getTag() != null) {
                File dir = new File(Utilities.getInstance().getModFolder(), "items");
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
                File file = new File(dir, System.currentTimeMillis()+".txt");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Id: ").append(Registry.ITEM.getId(itemStack.getItem()).toString()).append('\n');
                stringBuilder.append("Name: ").append(Text.Serializer.toJson(itemStack.getItem().getName())).append('\n');
                stringBuilder.append("Count: ").append(itemStack.getCount()).append('\n');
                stringBuilder.append("Tag: ").append(itemStack.getTag().toString()).append('\n');
                try {
                    if(file.createNewFile()) {
                        //noinspection UnstableApiUsage
                        Files.write(stringBuilder.toString(), file, StandardCharsets.UTF_8);
                        LiteralText text = new LiteralText("");
                        text.append(itemStack.toHoverableText());
                        text.append(" has been saved to ");
                        LiteralText filePathText = new LiteralText(file.getName());
                        filePathText.setStyle(Style.EMPTY
                                .withFormatting(Formatting.UNDERLINE, Formatting.AQUA)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()))
                        );
                        text.append(filePathText);
                        playerEntity.sendMessage(text, false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                info.setReturnValue(itemStack);
            }
        }
    }
}
