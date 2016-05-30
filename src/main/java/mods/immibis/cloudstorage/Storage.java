package mods.immibis.cloudstorage;

import java.util.HashMap;
import java.util.Map;

import mods.immibis.core.api.util.NBTType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.fluids.FluidRegistry;

public final class Storage extends WorldSavedData {
	
	public Map<Integer, Long> items = new HashMap<Integer, Long>();
	public Map<Integer, Long> fluids = new HashMap<Integer, Long>();
	public Map<CloudActionCoords, CloudAction> actions = new HashMap<CloudActionCoords, CloudAction>();
	public int storedRF = 0;
	public int storedEU = 0;
	public float storedMJ = 0;
	
	public Storage(String name) {
		super(name);
	}
	
	
	public void add(int packedID, int qty)
	{
		Long old = items.get(packedID);
		items.put(packedID, (old == null ? qty : old + qty));
	}
	
	public void addFluid(int fluidID, int qty)
	{
		Long old = fluids.get(fluidID);
		fluids.put(fluidID, (old == null ? qty : old + qty));
	}

	public static int packItemID(int id, int damage)
	{
		return (id << 16) | (damage & 65535);
	}
	
	public static int packItemID(ItemStack s)
	{
		return packItemID(Item.getIdFromItem(s.getItem()), s.getItemDamage());
	}
	
	public static int unpackItemID(int packed)
	{
		return (packed >> 16) & 65535;
	}
	
	public static int unpackDamage(int packed) 
	{
		return packed & 65535;
	}
	
	public static ItemStack unpackItemStack(int packed)
	{
		return new ItemStack(Item.getItemById(unpackItemID(packed)), 1, unpackDamage(packed));
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		NBTTagList list = tag.getTagList("items", NBTType.COMPOUND);
		for(int k = 0; k < list.tagCount(); k++)
		{
			NBTTagCompound c = list.getCompoundTagAt(k);
			items.put(c.getInteger("id"), c.getLong("qty"));
		}
		
		list = tag.getTagList("fluids", NBTType.COMPOUND);
		for(int k = 0; k < list.tagCount(); k++)
		{
			NBTTagCompound c = list.getCompoundTagAt(k);
			fluids.put(FluidRegistry.getFluidID(c.getString("id")), c.getLong("qty"));
		}
		
		storedRF = tag.getInteger("storedRF");
		storedEU = tag.getInteger("storedEU");
		storedMJ = tag.getFloat("storedMJ");
		
		list = tag.getTagList("actions", NBTType.COMPOUND);
		for(int k = 0; k < list.tagCount(); k++)
		{
			NBTTagCompound c = list.getCompoundTagAt(k);
			
			try {
				CloudAction action = CloudActionType.valueOf(c.getString("type")).clazz.newInstance();
				action.load(c);
				CloudActionCoords key = new CloudActionCoords(c.getInteger("x"), c.getInteger("y"), c.getInteger("z"), c.getInteger("side"), c.getInteger("dimension"));
				actions.put(key, action);
				
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		
		tag.setInteger("storedRF", storedRF);
		tag.setInteger("storedEU", storedEU);
		tag.setFloat("storedMJ", storedMJ);
		
		NBTTagList list = new NBTTagList();
		tag.setTag("items", list);
		for(Map.Entry<Integer, Long> entry : items.entrySet())
		{
			NBTTagCompound c = new NBTTagCompound();
			c.setInteger("id", entry.getKey());
			c.setLong("qty", entry.getValue());
			list.appendTag(c);
		}
		
		list = new NBTTagList();
		tag.setTag("fluids", list);
		for(Map.Entry<Integer, Long> entry : fluids.entrySet())
		{
			NBTTagCompound c = new NBTTagCompound();
			c.setString("id", FluidRegistry.getFluidName(entry.getKey()));
			c.setLong("qty", entry.getValue());
			list.appendTag(c);
		}
		
		list = new NBTTagList();
		tag.setTag("actions", list);
		for(Map.Entry<CloudActionCoords, CloudAction> entry : actions.entrySet())
		{
			CloudActionCoords key = entry.getKey();
			NBTTagCompound c = new NBTTagCompound();
			entry.getValue().save(c);
			c.setString("type", entry.getValue().getType().name());
			c.setInteger("x", key.x);
			c.setInteger("y", key.y);
			c.setInteger("z", key.z);
			c.setInteger("side", key.side);
			c.setInteger("dimension", key.dimension);
			list.appendTag(c);
		}
	}


	public static boolean canStore(ItemStack par1ItemStack) {
		return !par1ItemStack.hasTagCompound() || par1ItemStack.getTagCompound().hasNoTags();
	}


	public long getQty(int packedID) {
		Long l = items.get(packedID);
		return l == null ? 0 : l;
	}
	
	public void remove(int packedID, int qty)
	{
		Long l = items.get(packedID);
		if(l == null)
			throw new AssertionError("packed ID "+packedID+", qty "+qty+", existing 0");
		
		if(l < qty)
			throw new AssertionError("packed ID "+packedID+", qty "+qty+", existing "+l);
		
		if(l == qty)
			items.remove(packedID);
		else
			items.put(packedID, l - qty);
	}


	public long getFluidQty(int fluidID) {
		Long l = fluids.get(fluidID);
		return l == null ? 0L : l;
	}


	public void removeFluid(int fluidID, int qty) {
		Long l = fluids.get(fluidID);
		if(l == null)
			throw new AssertionError("fluid ID "+fluidID+", qty "+qty+", existing 0");
		
		if(l < qty)
			throw new AssertionError("fluid ID "+fluidID+", qty "+qty+", existing "+l);
		
		if(l == qty)
			fluids.remove(fluidID);
		else
			fluids.put(fluidID, l - qty);
	}
}
