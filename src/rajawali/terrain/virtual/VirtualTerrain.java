package rajawali.terrain.virtual;

import rajawali.terrain.TerrainLOD;
import rajawali.util.RajLog;

public class VirtualTerrain extends TerrainLOD {

	private boolean lazyLoad = true;
		
	private double latitude;
	
	private double longitude;
	
	public VirtualTerrain() {}

	public VirtualTerrain(boolean lazyLoad) {
		this.lazyLoad = lazyLoad;
	}

	
	public double getLatitude() {
		return latitude;
	}

	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	
	public double getLongitude() {
		return longitude;
	}

	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	
	public boolean isLazyLoad() {
		return lazyLoad;
	}
	
//	@Override
//	protected void preRender() {
//		super.preRender();
//		RajLog.d("VT:"+getName()+" Numm ho levels "+mLevels.size());
//		RajLog.d("VT:"+getName()+" Numm ho childs "+mChildren.size());
//	}
	
}
