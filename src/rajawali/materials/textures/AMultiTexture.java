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
package rajawali.materials.textures;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import rajawali.util.RajLog;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.GLES20;


public abstract class AMultiTexture extends ATexture {
	protected Bitmap[] mBitmaps;
	protected ByteBuffer[] mByteBuffers;
	protected int[] mResourceIds;

	protected AMultiTexture() {
		super();
	}

	public AMultiTexture(TextureType textureType, String textureName) {
		super(textureType, textureName);
	}
	
	public AMultiTexture(TextureType textureType, String textureName, int[] resourceIds)
	{
		super(textureType, textureName);
		setResourceIds(resourceIds);		
	}
	
	public AMultiTexture(TextureType textureType, String textureName, Bitmap[] bitmaps)
	{
		super(textureType, textureName);
		setBitmaps(bitmaps);
	}

	public AMultiTexture(TextureType textureType, String textureName, ByteBuffer[] byteBuffers)
	{
		super(textureType, textureName);
		setByteBuffers(byteBuffers);
	}

	public AMultiTexture(ATexture other) {
		super(other);
	}
	
	/**
	 * Copies every property from another AMultiTexture object
	 * 
	 * @param other
	 *            another AMultiTexture object to copy from
	 */
	public void setFrom(AMultiTexture other)
	{
		super.setFrom(other);
		setBitmaps(mBitmaps);
		setResourceIds(mResourceIds);
		setByteBuffers(mByteBuffers);
	}
	
	public void setResourceIds(int[] resourceIds)
	{
		mResourceIds = resourceIds;
		int numResources = resourceIds.length;
		mBitmaps = new Bitmap[numResources];
		mByteBuffers = new ByteBuffer[resourceIds.length];
		Context context = TextureManager.getInstance().getContext();

		for(int i=0; i<numResources; i++)
		{
			mBitmaps[i] = BitmapFactory.decodeResource(context.getResources(), resourceIds[i]); 
			if (i == 0) {
				setWidth(mBitmaps[i].getWidth());
				setHeight(mBitmaps[i].getHeight());
			}
		}
	}	
	
	
	public int[] getResourceIds()
	{
		return mResourceIds;
	}
	
	public void setBitmaps(Bitmap[] bitmaps)
	{
		mBitmaps = bitmaps;
	}
	
	public Bitmap[] getBitmaps()
	{
		return mBitmaps;
	}
	
	public void setByteBuffers(ByteBuffer[] byteBuffers)
	{
		
		int numResources = byteBuffers.length;
		mByteBuffers = new ByteBuffer[byteBuffers.length];

		for(int i=0; i<numResources; i++)
		{ 
			mByteBuffers[i] = byteBuffers[i];
			if (i == 0) {
				Bitmap bitmap = BitmapFactory.decodeByteArray(byteBuffers[i].array(), 0, byteBuffers[i].array().length);
				setWidth(bitmap.getWidth());
				setHeight(bitmap.getHeight());
			}
		}

	}
	
	public ByteBuffer[] getByteBuffers()
	{
		return mByteBuffers;
	}
	
	void reset() throws TextureException
	{
		if(mBitmaps != null)
		{
			int count = mBitmaps.length;
			for(int i=0; i<count; i++)
			{
				Bitmap bitmap = mBitmaps[i];
				bitmap.recycle();
				bitmap = null;
				mBitmaps[i] = null;
			}
		}
		if(mByteBuffers != null)
		{
			int count = mByteBuffers.length;
			for(int i=0; i<count; i++)
			{
				ByteBuffer byteBuffer = mByteBuffers[i];
				byteBuffer.clear();
				byteBuffer = null;
				mByteBuffers[i] = null;
			}
		}
	}
}
