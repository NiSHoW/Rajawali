package rajawali.terrain.virtual;

import java.util.TreeMap;

import rajawali.Object3D;
import rajawali.math.vector.Vector3;
import rajawali.terrain.TerrainLOD;
import rajawali.util.RajLog;


/**
 * Segment or terrain rappresented by an elevation grid
 * You TerrainLoader for load this kind of object
 * @author Nicola Avancini (nicola.avancini@gmail.com)
 */
public class VirtualTerrainSegment extends TerrainLOD{
	
	/**
	 * Return tree of loaded models
	 * @return
	 */
	public TreeMap<Double, Object3D> getLoadedLevels(){

		Object3D level;
		TreeMap<Double, Object3D> loadedLevels = 
				new TreeMap<Double, Object3D>();
		
		synchronized (mLevels) {
			for(double key: mLevels.keySet()){
				level = mLevels.get(key); 
				if(((IVirtualTerrainModel) level).isLoaded()){
					loadedLevels.put(key, level);					
				}	
			}
		}
		
		return loadedLevels;
	}
	
	/**
	 * Get better levels for specificated distance
	 * @param distance from point
	 * @return Best {@link Object3D} model
	 */
	public Object3D getLevelForDistance(double distance) {
		Object3D level = null;
		synchronized (mLevels) {
			Double key = mLevels.ceilingKey(distance);
			if(key != null){
				 level = mLevels.get(key); 
			}
		}
		
		if(level instanceof IVirtualTerrainModel){
			if(!((IVirtualTerrainModel) level).isLoaded()){
				VirtualTerrainManager.getInstance().load((IVirtualTerrainModel)level);
				TreeMap<Double, Object3D> loadedLevels = getLoadedLevels();
				Double key = loadedLevels.ceilingKey(distance);
				if(key != null){
					return loadedLevels.get(key);
				} else {
					return getBaseLevel();
				}
			}
		}
		
		return level;
	}
	
	
	/**
	 * Executed before the rendering process starts
	 */
	protected void preRender(double distance) {
		super.preRender(distance);

		//call TerrainManager Garbage for model to much far
		synchronized (mLevels) {
			for (Double key : mLevels.keySet()){
				if(mLevels.get(key) instanceof IVirtualTerrainModel){
					VirtualTerrainManager.getInstance()
						.garbageModels((IVirtualTerrainModel)mLevels.get(key), distance);	
				}
			}
		}
	}
	
}
