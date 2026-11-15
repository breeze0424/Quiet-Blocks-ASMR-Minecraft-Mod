package com.cmcplugins.asmr;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod("asmr")
public final class AsmrMod {
    public AsmrMod(FMLJavaModLoadingContext context) {
        AsmrNetwork.register();
        RegisterCommandsEvent.BUS.addListener(AsmrCommands::register);
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(AsmrServer::onLogout);
        if (FMLEnvironment.dist.isClient()) {
            context.registerConfig(ModConfig.Type.CLIENT, AsmrConfig.SPEC);
            ClientAsmr.register();
        }
    }
}
