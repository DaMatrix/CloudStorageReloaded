package mods.immibis.cloudstorage;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

public class CloudActionInsert extends CloudActionFiltered {

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
				changed |= stock(inv, slot, s, coords.side);
		}
		else
		{
			for(int slot = 0; slot < inv.getSizeInventory(); slot++)
				changed |= stock(inv, slot, s, -1);
		}
		if(changed)
			inv.markDirty();
		return true;
	}

	private boolean stock(IInventory inv, int slot, Storage s, int side) {
		ItemStack existing = inv.getStackInSlot(slot);
		if(existing != null)
		{
			if(!Storage.canStore(existing))
				return false;
			if(existing.stackSize >= existing.getMaxStackSize())
				return false;
			int id = Storage.packItemID(existing);
			if(!isInFilter(id))
				return false;
			if(s.getQty(id) <= 0)
				return false;
			existing.stackSize++;
			s.remove(id, 1);
			return true;
		}
		
		for(int id : filterIDs)
		{
			if(id == 0)
				continue;
			if(s.getQty(id) <= 0)
				continue;
			ItemStack stack = Storage.unpackItemStack(id);
			if(!inv.isItemValidForSlot(slot, stack))
				continue;
			if(side != -1 && !((ISidedInventory)inv).canInsertItem(slot, stack, side))
				continue;
			s.remove(id, 1);
			inv.setInventorySlotContents(slot, stack);
			return true;
		}
		return false;
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
