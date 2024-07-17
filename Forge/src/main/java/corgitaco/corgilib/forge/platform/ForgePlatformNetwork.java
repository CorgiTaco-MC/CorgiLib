package corgitaco.corgilib.forge.platform;

import corgitaco.corgilib.forge.network.ForgeNetworkHandler;
import corgitaco.corgilib.network.Packet;
import corgitaco.corgilib.platform.PlatformNetwork;
import net.minecraft.server.level.ServerPlayer;

public class ForgePlatformNetwork implements PlatformNetwork {
    @Override
    public <P extends Packet> void sendToClient(ServerPlayer player, P packet) {
        ForgeNetworkHandler.sendToPlayer(player, packet);
    }

    @Override
    public <P extends Packet> void sendToServer(P packet) {
        ForgeNetworkHandler.sendToServer(packet);
    }
}
