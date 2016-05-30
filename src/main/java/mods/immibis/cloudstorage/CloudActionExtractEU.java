package mods.immibis.cloudstorage;

import ic2.api.tile.IEnergyStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CloudActionExtractEU extends CloudAction {

	@Override
	public boolean apply(CloudActionCoords coords, World world, Storage s) {
		TileEntity te = world.getTileEntity(coords.x, coords.y, coords.z);
		if(!(te instanceof IEnergyStorage))
			return false;
		
		int max = Integer.MAX_VALUE - s.storedEU;
		if(max <= 0)
			return true;
		
		IEnergyStorage st = (IEnergyStorage)te;
		int used = Math.min(max, st.getStored());
		if(used <= 0)
			return true;
		
		st.setStored(st.getStored() - used);
		s.storedEU += used;
		s.setDirty(true);
		return true;
	}

	@Override
	public CloudActionType getType() {
		return CloudActionType.EXTRACT_EU;
	}
	
	@Override
	public String toString() {
		return "extract Energy Units";
	}

}
