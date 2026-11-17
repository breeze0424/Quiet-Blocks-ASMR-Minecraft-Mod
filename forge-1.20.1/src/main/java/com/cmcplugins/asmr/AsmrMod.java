package com.cmcplugins.asmr;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod("asmr")
public final class AsmrMod {
    public AsmrMod() {
        String version = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString();
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
            () -> new IExtensionPoint.DisplayTest(() -> version,
                (remoteVersion, isServer) -> remoteVersion == null
                    || version.equals(remoteVersion) || "1.3.5".equals(remoteVersion)));
        AsmrNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(AsmrCommands::register);
        MinecraftForge.EVENT_BUS.addListener(AsmrServer::onLogout);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AsmrConfig.SPEC);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientAsmr.register());
    }
}
