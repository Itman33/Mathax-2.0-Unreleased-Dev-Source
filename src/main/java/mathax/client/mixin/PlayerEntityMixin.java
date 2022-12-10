package mathax.client.mixin;

import mathax.client.MatHax;
import mathax.client.events.entity.DropItemsEvent;
import mathax.client.events.entity.player.ClipAtLedgeEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    protected void clipAtLedge(CallbackInfoReturnable<Boolean> infoReturnable) {
        ClipAtLedgeEvent event = MatHax.EVENT_BUS.post(ClipAtLedgeEvent.get());

        if (event.isSet()) {
            infoReturnable.setReturnValue(event.isClip());
        }
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void onDropItem(ItemStack stack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> info) {
        if (world.isClient && !stack.isEmpty()) {
            if (MatHax.EVENT_BUS.post(DropItemsEvent.get(stack)).isCancelled()) info.cancel();
        }
    }

    /*@Inject(method = "getBlockBreakingSpeed", at = @At(value = "RETURN"), cancellable = true)
    public void onGetBlockBreakingSpeed(BlockState block, CallbackInfoReturnable<Float> infoReturnable) {
        SpeedMine speedMine = Modules.get().get(SpeedMine.class);
        if (!speedMine.isEnabled() || speedMine.modeSetting.get() != SpeedMine.Mode.Normal) {
            return;
        }

        infoReturnable.setReturnValue((float) (infoReturnable.getReturnValue() * speedMine.modifier.get()));
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void dontJump(CallbackInfo info) {
        Anchor anchor = Modules.get().get(Anchor.class);
        if (anchor.isEnabled() && anchor.cancelJump) {
            info.cancel();
        }
    }*/
}
