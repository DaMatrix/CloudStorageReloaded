package mods.immibis.cloudstorage;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class CloudActionExtractFluid extends CloudAction {

	@Override
	public boolean apply(CloudActionCoords coords, World world, Storage s) {
		TileEntity te = world.getTileEntity(coords.x, coords.y, coords.z);
		if(!(te instanceof IFluidHandler))
			return false;
		
		IFluidHandler fh = (IFluidHandler)te;
		FluidStack drained = fh.drain(ForgeDirection.VALID_DIRECTIONS[coords.side], 10000, false);
		
		if(drained == null)
			return true;
		
		int fluidID = drained.getFluidID();
		
		if(s.getFluidQty(fluidID) > Integer.MAX_VALUE)
			return true;
		
		if(drained.tag == null || drained.tag.hasNoTags()) {
			drained = fh.drain(ForgeDirection.VALID_DIRECTIONS[coords.side], 10000, true);
			if(drained == null || (drained.tag != null && !drained.tag.hasNoTags()))
				throw new RuntimeException(te+" is returning a different fluid from drain with doDrain=false and doDrain=true");
			
			s.addFluid(fluidID, drained.amount);
			
			System.out.println("extracted "+drained.amount+" "+FluidRegistry.getFluidName(fluidID)+", now have "+s.getFluidQty(fluidID));
		}
		
		return true;
	}

	@Override
	public String toString() {
		return "extract all liquid";
	}
	
	@Override
	public CloudActionType getType() {
		return CloudActionType.EXTRACT;
	}
}
