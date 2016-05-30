package mods.immibis.cloudstorage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import mods.immibis.core.net.AbstractContainerSyncPacket;

public class PacketStorageInfo extends AbstractContainerSyncPacket {
	@Override
	public String getChannel() {
		return CloudStorage.CHANNEL;
	}
	
	@Override
	public byte getID() {
		return CloudStorage.S2C_STORAGE_GUI_SYNC;
	}
	
	public Map<Integer, Long> data;
	
	@Override
	public void read(DataInputStream in) throws IOException {
		try {
			data = (Map<Integer, Long>)new ObjectInputStream(in).readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e.toString(), e);
		}
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(data);
		oos.close();
	}
}
