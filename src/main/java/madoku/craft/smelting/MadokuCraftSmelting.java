package madoku.craft.smelting;

import madoku.craft.clock.MadokuClock;
import madoku.craft.chunk.ChunkManagerSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerLevel;
import madoku.craft.smelting.system.MadokuSmeltingManager;

public class MadokuCraftSmelting implements ModInitializer {
	public static final String MOD_ID = "madoku-craft-smelting";
	private static final String CHUNK_PROCESSOR_SMELTING_RUNTIME_ID = "madoku_smelting_runtime";

	private static final ChunkManagerSystem.ChunkProcessor SMELTING_CHUNK_RUNTIME_PROCESSOR =
		new ChunkManagerSystem.ChunkProcessor() {
			@Override
			public boolean requiresMotionColumns() {
				return false;
			}

			@Override
			public boolean requiresSurfaceColumns() {
				return false;
			}

			@Override
			public void discoverLoadedChunk(
				ServerLevel level,
				int chunkX,
				int chunkZ,
				ChunkManagerSystem.ChunkDiscoverySnapshot snapshot
			) {
				// No-op: keeps chunk runtime status refresh active for block-entity schedulers.
			}

			@Override
			public void processTrackedChunk(ServerLevel level, int chunkX, int chunkZ) {
				// No-op: smelting does not need per-chunk gameplay processing.
			}
		};

	@Override
	public void onInitialize() {
		ChunkManagerSystem.registerChunkProcessor(
			CHUNK_PROCESSOR_SMELTING_RUNTIME_ID,
			SMELTING_CHUNK_RUNTIME_PROCESSOR
		);
		MadokuSmeltingManager.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MadokuClock.reset();
			MadokuSmeltingManager.onServerStarted();
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			MadokuClock.reset();
			MadokuSmeltingManager.onServerStopped();
		});
	}
}
