package mods.immibis.cloudstorage;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

public class CloudActionExtract extends CloudAction {

	@Override
	public boolean apply(CloudActionCoords coords, World world, Storage s) {
		TileEntity te = world.getTileEntity(coords.x, coords.y, coords.z);
		if(!(te instanceof IInventory))
			return false;
		
		IInventory inv = (te instanceof TileEntityChest ? Blocks.chest.func_149951_m(world, coords.x, coords.y, coords.z) : (IInventory)te);
		boolean changed = false;
		if(inv instanceof ISidedInventory)
		{
			for(int slot : ((ISidedInventory)inv).getAccessibleSlotsFromSide(coords.side))
			{
				ItemStack stack = inv.getStackInSlot(slot);
				if(stack == null)
					continue;
				if(!((ISidedInventory)inv).canExtractItem(slot, stack, coords.side))
					continue;
				if(!Storage.canStore(stack))
					continue;
				s.add(Storage.packItemID(stack), stack.stackSize);
				inv.setInventorySlotContents(slot, null);
				changed = true;
			}
		}
		else
		{
			for(int slot = 0; slot < inv.getSizeInventory(); slot++)
			{
				ItemStack stack = inv.getStackInSlot(slot);
				if(stack == null)
					continue;
				if(!Storage.canStore(stack))
					continue;
				s.add(Storage.packItemID(stack), stack.stackSize);
				inv.setInventorySlotContents(slot, null);
				changed = true;
			}
		}
		if(changed)
			inv.markDirty();
		return true;
	}

	@Override
	public String toString() {
		return "extract all items";
	}
	
	@Override
	public CloudActionType getType() {
		return CloudActionType.EXTRACT;
	}
}
