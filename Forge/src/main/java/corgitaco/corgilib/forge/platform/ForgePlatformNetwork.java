package corgitaco.corgilib.forge.platform;

import com.google.auto.service.AutoService;
import corgitaco.corgilib.network.Packet;
import corgitaco.corgilib.forge.network.ForgeNetworkHandler;
import corgitaco.corgilib.platform.PlatformNetwork;
import net.minecraft.server.level.ServerPlayer;

@AutoService(PlatformNetwork.class)
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
