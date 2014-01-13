package rajawali.terrain.virtual;


public interface IVirtualTerrainModel {
	
	public void setDistance(double distance);
	
	public double getDistance();
	
	public boolean isLoaded();
	
	public void setLoaded(boolean loaded);
	
	public void destroy();
	
	public void load();
	
}
