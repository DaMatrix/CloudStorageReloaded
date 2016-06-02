package mods.immibis.cloudstorage;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class CloudActionInsertFluid extends CloudActionFiltered {

	@Override
	public boolean apply(CloudActionCoords coords, World world, Storage s) {
		TileEntity te = world.getTileEntity(coords.x, coords.y, coords.z);
		if(!(te instanceof IFluidHandler))
			return false;
		
		IFluidHandler ifh = (IFluidHandler)te;
		
		for(int i : filterIDs)
			if(i != 0) {
				FluidStack insertedFluid = FluidContainerRegistry.getFluidForFilledItem(Storage.unpackItemStack(i));
				if(insertedFluid == null)
					continue;
				
				if(stock(ifh, FluidRegistry.getFluidID(insertedFluid.getFluid().getName()), s, ForgeDirection.VALID_DIRECTIONS[coords.side]))
					break;
			}
		
		return true;
	}

	private boolean stock(IFluidHandler inv, int fluidID, Storage s, ForgeDirection side) {
		long avail = s.getFluidQty(fluidID);
		if(avail == 0)
			return false;
		
		int qty = (int)Math.min(avail, 10000);
		
		qty = inv.fill(side, new FluidStack(fluidID, qty), true);
		
		s.removeFluid(fluidID, qty);
		
		return true;
	}
	
	@Override
	public String toString() {
		return "insert items";
	}
	
	@Override
	public CloudActionType getType() {
		return CloudActionType.INSERT;
	}

}
