package rajawali;

import java.util.TreeMap;

import rajawali.bounds.BoundingBox;
import rajawali.materials.MaterialManager;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;
import rajawali.util.RajLog;

/**
 * Level of details implementation
 * @author Nicola Avancini (nicola.avancini@gmail.com)
 */
public class LOD extends Object3D {

	protected TreeMap<Double, Object3D> mLevels;	
	
	public LOD() {
		super();
		mLevels = new TreeMap<Double, Object3D>();
	}	
	
	/**
	 * Add base level used always thereis not better levels
	 * @param model
	 */
	public void addBaseLevel(Object3D model){
		addLevel(model, Double.MAX_VALUE);
	}	
	
	/**
	 * Add new level of details
	 * @param model
	 * @param distance
	 */
	public void addLevel(Object3D model, double distance){
		synchronized (mLevels) {
			mLevels.put(distance, model);
		}
		
		RajLog.d("Last key"+mLevels.lastKey());
	}
		
	/**
	 * get number of levels present
	 * @return
	 */
	public int getNumberOfLevels() {
		synchronized (mLevels) {
			return mLevels.size();
		}
	}
	
	/**
	 * Get the base levels
	 * @return
	 */
	public Object3D getBaseLevel() {
		synchronized (mLevels) {
			if(mLevels.containsKey(Double.MAX_VALUE)){				
				return mLevels.get(Double.MAX_VALUE);
			}			
		}	
		
		return null;
	}
	
	/**
	 * Get better levels for specificated distance
	 * @param distance from point
	 * @return Best {@link Object3D} model
	 */
	public Object3D getLevelForDistance(double distance) {
		synchronized (mLevels) {
			Double key = mLevels.ceilingKey(distance);
			if(key != null){
				return mLevels.get(key);		
			}
		}
		return this.getBaseLevel();
	}
	
	
	public boolean hasElements(){
		return (mChildren.size() + mLevels.size()) > 0;
	}
	
	
	/**
	 * Executed before the rendering process starts
	 */
	protected void preRender(double distance) {

	}
	
	
	/**
	 * Renders the object
	 * 
	 * @param camera The camera
	 * @param vpMatrix {@link Matrix4} The view-projection matrix
	 * @param projMatrix {@link Matrix4} The projection matrix
	 * @param vMatrix {@link Matrix4} The view matrix
	 * @param parentMatrix {@link Matrix4} This object's parent matrix
	 * @param pickerInfo The current color picker info. This is only used when an object is touched.
	 */
	public void render(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix, final Matrix4 vMatrix, 
			final Matrix4 parentMatrix, ColorPickerInfo pickerInfo) {
		
		if (!hasElements())
			return;
		
		if (!mIsVisible && !mRenderChildrenAsBatch)
			return;

		preRender();

		mParentMatrix = parentMatrix;
		// -- move view matrix transformation first
		calculateModelMatrix(parentMatrix);
		// -- calculate model view matrix;
		mMVMatrix.setAll(vMatrix).multiply(mMMatrix);
		//Create MVP Matrix from View-Projection Matrix
		mMVPMatrix.setAll(vpMatrix).multiply(mMMatrix);

		//calculate distance from centerpoing
		Vector3 center = new Vector3();
		center.setAll(getGeometry().getCenter()).multiply(mMMatrix);
		double distance = camera.getPosition().distanceTo(center);
		
		//call prerendere distance
		preRender(distance);
		
		
		mIsInFrustum = true; // only if mFrustrumTest == true it check frustum
		if (mFrustumTest && getGeometry().hasBoundingBox()) {
			BoundingBox bbox = mGeometry.getBoundingBox();
			bbox.transform(mMMatrix);
			if (!camera.mFrustum.boundsInFrustum(bbox)) {
				mIsInFrustum = false;
			}
		}

		//select model for actual distance
		Object3D level = getLevelForDistance(distance);				
		if(level != null){
			
//			RajLog.d("render level object:"+level);
//			RajLog.d("center level object:"+level.getGeometry().getCenter());
//			RajLog.d("min level object:"+level.getGeometry().getMinLimit());
//			RajLog.d("max level object:"+level.getGeometry().getMaxLimit());
			
			if(mOverrideMaterialColor){
				level.mOverrideMaterialColor = true;
				level.getMaterial().setColor(mColor);
			}
			
			level.render(camera, vpMatrix, projMatrix, vMatrix, mMMatrix, pickerInfo);
		}

		if (mShowBoundingVolume) {			
			if (getGeometry().hasBoundingBox())
				mGeometry.getBoundingBox().drawBoundingVolume(camera, vpMatrix, projMatrix, vMatrix, mMMatrix);
			if (getGeometry().hasBoundingSphere())
				mGeometry.getBoundingSphere().drawBoundingVolume(camera, vpMatrix, projMatrix, vMatrix, mMMatrix);
		}
		
		// Draw children without frustum test
		for (int i = 0, j = mChildren.size(); i < j; i++)
		{
			Object3D child = mChildren.get(i);
			if(mRenderChildrenAsBatch || mIsPartOfBatch)
				child.setPartOfBatch(true);
			child.render(camera, vpMatrix, projMatrix, vMatrix, mMMatrix, pickerInfo);
		}

	}
	
	/**
	 * clone object
	 * @return new {@link LOD} bject
	 */
	@SuppressWarnings("unchecked")
	public LOD clone(boolean copyMaterial) {
		LOD clone = new LOD();
		cloneTo(clone, copyMaterial);
		clone.mLevels = (TreeMap<Double, Object3D>) mLevels.clone();
		clone.setRotation(getRotation());
		clone.setScale(getScale());
		return clone;
	}
	
	
	/**
	 * get geometry of load calculate on base level
	 * @return {@link Geometry3D} for this lod
	 */
	public Geometry3D getGeometry() {
		
		if(!mGeometry.hasBounds()){
			Object3D level = getBaseLevel();
			
			if(level != null || mLevels.size() > 0){
				//RajLog.d("Calculate bounds from levels");
				//calculate geometry bounds from childs
				Vector3 lMin = new Vector3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
				Vector3 lMax = new Vector3(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
					
				if(level == null){
					synchronized (mLevels) {
						level = mLevels.get(mLevels.lastKey());
					}
				}
				
				Geometry3D levelGeom = level.getGeometry();
				if(levelGeom.getMinLimit().x < lMin.x) lMin.x = levelGeom.getMinLimit().x;
				if(levelGeom.getMinLimit().y < lMin.y) lMin.y = levelGeom.getMinLimit().y;
				if(levelGeom.getMinLimit().z < lMin.z) lMin.z = levelGeom.getMinLimit().z;
				if(levelGeom.getMaxLimit().x > lMax.x) lMax.x = levelGeom.getMaxLimit().x;
				if(levelGeom.getMaxLimit().y > lMax.y) lMax.y = levelGeom.getMaxLimit().y;
				if(levelGeom.getMaxLimit().z > lMax.z) lMax.z = levelGeom.getMaxLimit().z;
				
				//set manual bounds
				mGeometry.setBounds(lMin, lMax);				
			} else {
				super.getGeometry();
			}
		}
		
		return mGeometry;
	}
	
	
	/**
	 * Normalize Object or container from -1 to 1
	 */
	public void normalize(Matrix4 matrix) {				
		Object3D level = null;
		synchronized (mLevels) {
			TreeMap<Double, Object3D> newLevels = new TreeMap<Double, Object3D>();
			for(Double key : mLevels.keySet()){
				double newKey = key;
				level = mLevels.get(key);				
				if(key != Double.MAX_VALUE){
					//its same scale for x y and x
					double scale = matrix.getScaling().x;
					newKey = key / scale;
				}								
				level.normalize(matrix);
				newLevels.put(newKey, level);
			}			
			mLevels = newLevels;
		}
		
		super.normalize(matrix);
	}	
		
	
	/**
	 * Destroy element
	 */
	public void destroy() {
		if (mGeometry != null)
			mGeometry.destroy();
		if (mMaterial != null)
			MaterialManager.getInstance().removeMaterial(mMaterial);
		mMaterial = null;
		mGeometry = null;
		
		synchronized (mLevels) {
			for (Double key : mLevels.keySet())
				mLevels.get(key).destroy();
			mLevels.clear();
		}
		
		for (int i = 0, j = mChildren.size(); i < j; i++)
			mChildren.get(i).destroy();
		mChildren.clear();
	}
	

	public SerializedObject3D toSerializedObject3D() {
		return null;
	}
	
}
