package mods.immibis.cloudstorage;

import mods.immibis.core.BasicInventory;
import mods.immibis.core.api.net.IPacket;
import mods.immibis.core.api.util.BaseContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ContainerBrowser extends BaseContainer<Object> {
	
	public IInventory fakeInventory = new BasicInventory(6*8);
	public final int HEIGHT = 6, WIDTH = 8;
	public Storage storage;
	public int[] visibleIDs = new int[WIDTH*HEIGHT]; // updated by client
	
	public ContainerBrowser(EntityPlayer ply, int unused1, int unused2, int unused3)
	{
		super(ply, null);
		
		if(ply instanceof EntityPlayerMP)
			storage = CloudStorage.getStorage(ply.getDisplayName()); // TODO this is wrong still
		else
			storage = new Storage("");
		
		for(int x = 0; x < 9; x++)
			addSlotToContainer(new Slot(ply.inventory, x, 8 + 18*x, 198));
		
		for(int y = 0; y < 3; y++)
			for(int x = 0; x < 9; x++)
				addSlotToContainer(new Slot(ply.inventory, x + y*9 + 9, 8 + 18*x, 140 + 18*y));
		
		for(int y = 0; y < HEIGHT; y++)
			for(int x = 0; x < WIDTH; x++)
				addSlotToContainer(new SlotExtended(fakeInventory, x + y*WIDTH, 8 + 18*x, 20 + 18*y));
	}
	
	@Override
	public void onUpdatePacket(IPacket p) {
		if(p instanceof PacketStorageInfo)
		{
			System.out.println("received update packet");
			storage.items = ((PacketStorageInfo)p).data;
			needUpdateVisibleIDs = true;
		}
	}
	
	private int tickCounter;
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		
		if(tickCounter++ >= 40)
		{
			tickCounter = 0;
			
			sendUpdatePacket();
		}
	}
	
	private void sendUpdatePacket()
	{
		PacketStorageInfo p = new PacketStorageInfo();
		p.data = storage.items;
		sendUpdatePacket(p);
		System.out.println("send update packet");
	}
	
	@Override
	public void onActionPacket(IPacket p) {
		if(p instanceof PacketSetVisibleSlot)
		{
			PacketSetVisibleSlot psvs = (PacketSetVisibleSlot)p;
			visibleIDs[psvs.slot] = psvs.packedID;
		}
	}
	
	@Override
	public void addCraftingToCrafters(ICrafting par1iCrafting) {
		super.addCraftingToCrafters(par1iCrafting);
		
		if(par1iCrafting instanceof EntityPlayerMP)
			sendUpdatePacket();
	}
	
	public static class SlotExtended extends Slot
	{

		public SlotExtended(IInventory par1iInventory, int par2, int par3, int par4) {
			super(par1iInventory, par2, par3, par4);
		}
		
		@Override
		public boolean isItemValid(ItemStack par1ItemStack) {
			return Storage.canStore(par1ItemStack);
		}
		
	}
	
	boolean needUpdateVisibleIDs = false;
	
	@Override
	public ItemStack slotClick(int slot, int button, int shift, EntityPlayer player) {
		if(slot < 36)
		{
			if(shift == 1)
			{
				// shift click
				Slot slotObject = (Slot)inventorySlots.get(slot);
				ItemStack stack = slotObject.getStack();
				if(stack == null || !Storage.canStore(stack))
					return null;
				storage.add(Storage.packItemID(stack), stack.stackSize);
				slotObject.putStack(null);
				return null;
			}
			return super.slotClick(slot, button, shift, player);
		}
		
		slot -= 36;
		
		if(player.worldObj.isRemote)
		{
			PacketSetVisibleSlot p = new PacketSetVisibleSlot();
			p.slot = slot;
			p.packedID = visibleIDs[slot];
			sendActionPacket(p);
		
		} else
			storage.setDirty(true);
		
		if(player.inventory.getItemStack() != null)
		{
			ItemStack s = player.inventory.getItemStack();
			if(!Storage.canStore(s))
				return null;
			
			storage.add(Storage.packItemID(s), s.stackSize);
			player.inventory.setItemStack(null);
			
			needUpdateVisibleIDs = true;
			
			return null;
		}
		
		int packedID = visibleIDs[slot];
		if(packedID == 0)
			return null;
		
		ItemStack retStack = new ItemStack(Item.getItemById(Storage.unpackItemID(packedID)), 1, Storage.unpackDamage(packedID));
		int removeAmt = (int)Math.min(retStack.getMaxStackSize(), storage.getQty(packedID));
		if(removeAmt == 0)
			return null;
		
		storage.remove(packedID, removeAmt);
		retStack.stackSize = removeAmt;
		
		if(shift == 1)
		{
			retStack = BasicInventory.mergeStackIntoRange(retStack, player.inventory, 0, 36);
			if(retStack != null && retStack.stackSize > 0)
				storage.add(packedID, retStack.stackSize);
		}
		else
			player.inventory.setItemStack(retStack);
		
		needUpdateVisibleIDs = true;
		
		return null;
	}
}
