package mods.immibis.cloudstorage;

import java.io.DataOutputStream;
import java.io.IOException;

import mods.immibis.core.net.AbstractContainerSyncPacket;

// The existence of this packet is a hack.
public class PacketSetVisibleSlot extends AbstractContainerSyncPacket {
	public int slot, packedID;
	
	@Override
	public String getChannel() {
		return CloudStorage.CHANNEL;
	}
	
	@Override
	public byte getID() {
		return CloudStorage.C2S_SET_VISIBLE_SLOT;
	}
	
	@Override
	public void read(java.io.DataInputStream in) throws IOException {
		slot = in.readInt();
		packedID = in.readInt();
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(slot);
		out.writeInt(packedID);
	}
}
