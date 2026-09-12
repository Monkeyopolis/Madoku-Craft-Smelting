package madoku.craft.java.utility.music;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;

/** A music instance bound directly to one resolved OGG resource. */
public final class MadokuMusicSoundInstance extends AbstractSoundInstance {
	private final WeighedSoundEvents resolvedEvent;

	public MadokuMusicSoundInstance(Identifier eventId, Identifier soundLocation, float volume) {
		super(eventId, SoundSource.MUSIC, RandomSource.create());
		this.sound = new Sound(
			soundLocation,
			ConstantFloat.of(1.0F),
			ConstantFloat.of(1.0F),
			1,
			Sound.Type.FILE,
			true,
			false,
			16
		);
		this.volume = volume;
		this.pitch = 1.0F;
		this.attenuation = SoundInstance.Attenuation.NONE;
		this.relative = true;
		this.resolvedEvent = new WeighedSoundEvents(eventId, "subtitles.madoku_craft.music");
	}

	@Override
	public WeighedSoundEvents resolve(SoundManager soundManager) {
		return resolvedEvent;
	}
}
