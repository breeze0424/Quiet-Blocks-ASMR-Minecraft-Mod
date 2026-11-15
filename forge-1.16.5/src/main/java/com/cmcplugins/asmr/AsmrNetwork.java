package com.cmcplugins.asmr;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class AsmrNetwork {
    private static final String VERSION = "5";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("asmr", "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private AsmrNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, AsmrPacket.class, AsmrPacket::encode, AsmrPacket::decode, (message, context) -> {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    if (message.openMenu) ClientAsmr.openMenu();
                    else ClientAsmr.receive(message);
                }));
            context.get().setPacketHandled(true);
        });
    }

    public static void send(net.minecraft.entity.player.ServerPlayerEntity player, AsmrPacket message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
