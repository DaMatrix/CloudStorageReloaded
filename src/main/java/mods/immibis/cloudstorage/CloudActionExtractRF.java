package mods.immibis.cloudstorage;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class CloudActionExtractRF extends CloudAction {

	@Override
	public boolean apply(CloudActionCoords coords, World world, Storage s) {
		TileEntity te = world.getTileEntity(coords.x, coords.y, coords.z);
		if(!(te instanceof IEnergyHandler))
			return false;
		
		if(s.storedRF < 0)
		{
			System.out.println("shouldn't happen: negative stored RF: "+s.storedRF);
			s.storedRF = 0;
		}
		
		int max = Integer.MAX_VALUE - s.storedRF;
		if(max <= 0)
			return true;
		
		ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[coords.side];
		int used = ((IEnergyHandler)te).extractEnergy(dir, max, false);
		if(used > max)
			used = max;
		if(used <= 0)
			return true;
		
		//System.out.println("adding "+used+" RF, now storing "+(s.storedRF+used));
		
		s.storedRF += used;
		s.setDirty(true);
		return true;
	}

	@Override
	public CloudActionType getType() {
		return CloudActionType.EXTRACT_RF;
	}
	
	@Override
	public String toString() {
		return "extract Redstone Flux";
	}

}
