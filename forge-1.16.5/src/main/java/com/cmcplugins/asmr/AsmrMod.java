package com.cmcplugins.asmr;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod("asmr")
public final class AsmrMod {
    public AsmrMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AsmrConfig.SPEC);
        AsmrNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(AsmrCommands::register);
        MinecraftForge.EVENT_BUS.addListener(AsmrServer::onLogout);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientAsmr.register());
    }
}
