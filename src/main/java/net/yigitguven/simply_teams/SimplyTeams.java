package net.yigitguven.simply_teams;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yigitguven.simply_teams.event.TeamEventHandler;

@Mod(SimplyTeams.MODID)
public class SimplyTeams {
    public static final String MODID = "simply_teams";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SimplyTeams(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        NeoForge.EVENT_BUS.register(TeamEventHandler.class);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Simply Teams (Server-Sided) initialized successfully!");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        // Marks Simply Teams network as completely optional for connecting clients.
        // Unmodded / vanilla Minecraft clients can connect without the mod installed!
        event.registrar(MODID).optional();
    }
}
