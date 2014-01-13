/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali.bounds;

import rajawali.Object3D;
import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.math.Matrix4;

public interface IBoundingVolume {
	
	public static final int DEFAULT_COLOR = 0xFFFFFF00;
	
	@Deprecated
	/**
	 * Deprecated method for calculate bounding limits,
	 * moved to geometry
	 */
	public void calculateBounds(Geometry3D geometry);
	
	/**
	 * render function for bounding volume
	 */
	public void drawBoundingVolume(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix, 
			final Matrix4 vMatrix, final Matrix4 mMatrix);
	
	/**
	 * trasform implementation
	 */
	public void transform(Matrix4 matrix);
	
	/**
	 * Check intersection whit another boundigVolume (BoundingBox o Sphere)
	 */
	public boolean intersectsWith(IBoundingVolume boundingVolume);
		
	/**
	 * Return Visual rappresentation of boundingVolume
	 */
	public Object3D getVisual();
	
	/**
	 * Set color
	 */
	public void setBoundingColor(int color);
	
	/**
	 * get setted Color
	 */
	public int getBoundingColor();
}

