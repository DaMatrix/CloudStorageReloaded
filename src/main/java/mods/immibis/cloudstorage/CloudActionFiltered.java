package mods.immibis.cloudstorage;

import net.minecraft.nbt.NBTTagCompound;

public abstract class CloudActionFiltered extends CloudAction {
	public int filterIDs[] = new int[6*9];
	
	public boolean isInFilter(int id)
	{
		for(int i : filterIDs)
			if(i == id)
				return true;
		return false;
	}
	
	@Override
	public void load(NBTTagCompound c) {
		super.load(c);
		for(int k = 0; k < filterIDs.length; k++)
			filterIDs[k] = c.getInteger(String.valueOf(k));
	}
	
	@Override
	public void save(NBTTagCompound c) {
		super.save(c);
		for(int k = 0; k < filterIDs.length; k++)
			c.setInteger(String.valueOf(k), filterIDs[k]);
	}
}
