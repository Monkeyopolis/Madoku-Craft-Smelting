package madoku.craft.mixin.music;

import madoku.craft.java.utility.music.MusicAPIManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public final class MusicManagerMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$overrideVanillaMusicTick(CallbackInfo callbackInfo) {
		Minecraft client = Minecraft.getInstance();
		boolean customTickHandled = MusicAPIManager.tick(client);
		boolean ownsMusicContext = customTickHandled || MusicAPIManager.overridesVanillaMusic(client);
		if (ownsMusicContext) {
			callbackInfo.cancel();
		}
	}

	@Inject(method = "stopPlaying()V", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$preventVanillaStop(CallbackInfo callbackInfo) {
		if (MusicAPIManager.overridesVanillaMusic(Minecraft.getInstance())) {
			callbackInfo.cancel();
		}
	}

	@Inject(method = "stopPlaying(Lnet/minecraft/sounds/Music;)V", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$preventVanillaStop(Music music, CallbackInfo callbackInfo) {
		if (MusicAPIManager.overridesVanillaMusic(Minecraft.getInstance())) {
			callbackInfo.cancel();
		}
	}

	@Inject(method = "startPlaying(Lnet/minecraft/sounds/Music;)V", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$preventVanillaStart(Music music, CallbackInfo callbackInfo) {
		if (MusicAPIManager.overridesVanillaMusic(Minecraft.getInstance())) {
			callbackInfo.cancel();
		}
	}
}
