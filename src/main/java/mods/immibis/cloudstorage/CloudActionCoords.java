package mods.immibis.cloudstorage;

public final class CloudActionCoords {
	public final int x, y, z, side, dimension;
	
	public CloudActionCoords(int x, int y, int z, int side, int dimension)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.side = side;
		this.dimension = dimension;
	}

	@Override
	public int hashCode() {
		return (x + (side * 31)) + (y + (z * 31)) * (31 * 31);
	}

	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof CloudActionCoords) {
			CloudActionCoords o = (CloudActionCoords)arg0;
			return o.x == x && o.y == y && o.z == z && o.side == side && o.dimension == dimension;
		}
		return false;
	}
	
}
