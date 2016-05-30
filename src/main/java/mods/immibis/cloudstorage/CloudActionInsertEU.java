package mods.immibis.cloudstorage;

import ic2.api.energy.tile.IEnergySink;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class CloudActionInsertEU extends CloudAction {
	@Override
	public boolean apply(CloudActionCoords coords, World world, Storage s) {
		TileEntity te = world.getTileEntity(coords.x, coords.y, coords.z);
		if(!(te instanceof IEnergySink))
			return false;
		
		IEnergySink sink = (IEnergySink)te;
		int max = Math.min(s.storedEU, (int)Math.min(1 << (5+2*sink.getSinkTier()), sink.getDemandedEnergy()));
		if(max <= 0)
			return true;
		
		int used = max - (int)(sink.injectEnergy(ForgeDirection.VALID_DIRECTIONS[coords.side], max, 1) + world.rand.nextDouble());
		if(used <= 0)
			return true;
		
		s.storedEU -= used;
		s.setDirty(true);
		return true;
	}

	@Override
	public CloudActionType getType() {
		return CloudActionType.EXTRACT_EU;
	}
	
	@Override
	public String toString() {
		return "insert Energy Units";
	}
}
