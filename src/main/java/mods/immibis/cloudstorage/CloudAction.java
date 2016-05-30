package mods.immibis.cloudstorage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class CloudAction {
	// Returns false if this action is invalid.
	public abstract boolean apply(CloudActionCoords coords, World world, Storage s);
	
	public abstract CloudActionType getType();
	public void load(NBTTagCompound c) {}
	public void save(NBTTagCompound c) {}
}
