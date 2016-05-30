package mods.immibis.cloudstorage;

import mods.immibis.core.SlotFake;
import mods.immibis.core.api.util.BaseContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerFilter extends BaseContainer<IInventory> {
	public static int[] nextFilterIDArray;
	public int[] filterIDs;
	
	public final IInventory filterInv = new IInventory() {
		@Override
		public void setInventorySlotContents(int i, ItemStack itemstack) {
			if(itemstack == null)
				filterIDs[i] = 0;
			else if(Storage.canStore(itemstack))
				filterIDs[i] = Storage.packItemID(itemstack); 
		}
		
		@Override
		public void openInventory() {
		}
		
		@Override
		public void markDirty() {
		}
		
		@Override
		public boolean isUseableByPlayer(EntityPlayer entityplayer) {
			return true;
		}
		
		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemstack) {
			return true;
		}
		
		@Override
		public boolean hasCustomInventoryName() {
			return true;
		}
		
		@Override
		public ItemStack getStackInSlotOnClosing(int i) {
			return null;
		}
		
		@Override
		public ItemStack getStackInSlot(int i) {
			return filterIDs[i] == 0 ? null : Storage.unpackItemStack(filterIDs[i]);
		}
		
		@Override
		public int getSizeInventory() {
			return filterIDs.length;
		}
		
		@Override
		public int getInventoryStackLimit() {
			return 64;
		}
		
		@Override
		public String getInventoryName() {
			return "";
		}
		
		@Override
		public ItemStack decrStackSize(int i, int j) {
			return null;
		}
		
		@Override
		public void closeInventory() {
		}
	};
	
	public ContainerFilter(EntityPlayer player, int unused1, int unused2, int unused3)
	{
		super(player, null);
		
		if(player.worldObj.isRemote)
			filterIDs = new int[6*9];
		else if(nextFilterIDArray != null) {
			filterIDs = nextFilterIDArray;
			nextFilterIDArray = null;
		} else
			filterIDs = new int[6*9];
		
		for(int x = 0; x < 9; x++)
			addSlotToContainer(new Slot(player.inventory, x, 8 + 18*x, 198));
		
		for(int y = 0; y < 3; y++)
			for(int x = 0; x < 9; x++)
				addSlotToContainer(new Slot(player.inventory, x + y*9 + 9, 8 + 18*x, 140 + 18*y));
		
		for(int y = 0; y < 6; y++)
			for(int x = 0; x < 9; x++)
				addSlotToContainer(new SlotFake(filterInv, x + y*9, 8 + 18*x, 20 + 18*y));
	}

	public String getNameText() {
		return "Filter";
	}
}
