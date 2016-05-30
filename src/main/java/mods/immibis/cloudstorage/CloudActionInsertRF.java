package mods.immibis.cloudstorage;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class CloudActionInsertRF extends CloudAction {
	@Override
	public boolean apply(CloudActionCoords coords, World world, Storage s) {
		TileEntity te = world.getTileEntity(coords.x, coords.y, coords.z);
		if(!(te instanceof IEnergyHandler))
			return false;
		
		ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[coords.side];
		int used = ((IEnergyHandler)te).receiveEnergy(dir, s.storedRF, false);
		if(used <= 0)
			return true;
		
		s.storedRF -= used;
		s.setDirty(true);
		return true;
	}

	@Override
	public CloudActionType getType() {
		return CloudActionType.INSERT_RF;
	}
	
	@Override
	public String toString() {
		return "insert Redstone Flux";
	}
}
