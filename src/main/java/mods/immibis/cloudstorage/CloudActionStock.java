package mods.immibis.cloudstorage;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class CloudActionStock extends CloudAction {
	
	public int[] ids;
	
	public CloudActionStock(CloudActionCoords coords, Storage s)
	{
		World world = DimensionManager.getWorld(coords.dimension);
		TileEntity te = world.getTileEntity(coords.x, coords.y, coords.z);
		if(!(te instanceof IInventory))
		{
			ids = new int[0];
			return;
		}
		
		IInventory inv = (te instanceof TileEntityChest ? Blocks.chest.func_149951_m(world, coords.x, coords.y, coords.z) : (IInventory)te);
		if(inv instanceof ISidedInventory)
		{
			int[] slots = ((ISidedInventory)inv).getAccessibleSlotsFromSide(coords.side);
			ids = new int[slots.length];
			for(int k = 0; k < slots.length; k++)
			{
				ItemStack stack = inv.getStackInSlot(slots[k]);
				if(stack == null)
					continue;
				if(!Storage.canStore(stack))
					continue;
				ids[k] = Storage.packItemID(stack);
			}
		}
		else
		{
			ids = new int[inv.getSizeInventory()];
			for(int slot = 0; slot < inv.getSizeInventory(); slot++)
			{
				ItemStack stack = inv.getStackInSlot(slot);
				if(stack == null)
					continue;
				if(!Storage.canStore(stack))
					continue;
				ids[slot] = Storage.packItemID(stack);
			}
		}
		//System.out.println("stock IDs: "+Arrays.toString(ids));
	}
	
	@Override
	public boolean apply(CloudActionCoords coords, World world, Storage s) {
		TileEntity te = world.getTileEntity(coords.x, coords.y, coords.z);
		if(!(te instanceof IInventory))
			return false;
		
		IInventory inv = (te instanceof TileEntityChest ? Blocks.chest.func_149951_m(world, coords.x, coords.y, coords.z) : (IInventory)te);
		boolean changed = false;
		if(inv instanceof ISidedInventory)
		{
			int[] slots = ((ISidedInventory)inv).getAccessibleSlotsFromSide(coords.side);
			for(int k = 0; k < slots.length && k < ids.length; k++)
				changed |= stock(inv, slots[k], ids[k], s);
		}
		else
		{
			for(int slot = 0; slot < inv.getSizeInventory() && slot < ids.length; slot++)
				changed |= stock(inv, slot, ids[slot], s);
		}
		if(changed)
			inv.markDirty();
		return true;
	}

	private boolean stock(IInventory inv, int slot, int id, Storage s) {
		if(id == 0 || s.getQty(id) <= 0)
			return false;
		
		ItemStack stack = inv.getStackInSlot(slot);
		if(stack != null && (!Storage.canStore(stack) || Storage.packItemID(stack) != id))
			return false;
		
		if(stack == null)
			inv.setInventorySlotContents(slot, Storage.unpackItemStack(id));
		else if(stack.stackSize >= stack.getMaxStackSize())
			return false;
		else
			stack.stackSize++;
		s.remove(id, 1);
		
		return true;
	}
	
	@Override
	public String toString() {
		return "stock items (this is supposed to be disabled, how did you get this?)";
	}
	
	@Override
	public CloudActionType getType() {
		return CloudActionType.STOCK;
	}
}
