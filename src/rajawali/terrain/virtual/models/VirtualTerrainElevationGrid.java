package rajawali.terrain.virtual.models;

import android.graphics.Color;
import rajawali.Object3D;
import rajawali.materials.Material;
import rajawali.math.vector.Vector3;
import rajawali.primitives.PointSprite;
import rajawali.terrain.virtual.IVirtualTerrainModel;
import rajawali.util.RajLog;


public class VirtualTerrainElevationGrid extends Object3D implements IVirtualTerrainModel{
	
	private double distance;
	
	private int xDimension = 1;
	private int yDimension = 1;
	private double xSpacing = 1;
	private double ySpacing = 1;
	private float[] elevationMatrix;
	private float[] textureCoords;
	
	private boolean isLoaded = false;
	
	
	public VirtualTerrainElevationGrid() {}
		

	public VirtualTerrainElevationGrid(int xDimension, int yDimension, float[] elevationMatrix){
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.elevationMatrix = elevationMatrix;
	}

	
	public VirtualTerrainElevationGrid(int xDimension, int yDimension, 
			double xSpacing, double ySpacing, float[] elevationMatrix, float[] textureCoords){
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.xSpacing = xSpacing;		
		this.ySpacing = ySpacing;
		this.elevationMatrix = elevationMatrix;
		this.textureCoords = textureCoords;
	}
	
	public int getxDimension() {
		return xDimension;
	}
	
	public void setxDimension(int xDimension) {
		this.xDimension = xDimension;
	}
	
	public int getyDimension() {
		return yDimension;
	}
	
	public void setyDimension(int yDimension) {
		this.yDimension = yDimension;
	}
	
	public double getxSpacing() {
		return xSpacing;
	}
	
	public void setxSpacing(double xSpacing) {
		this.xSpacing = xSpacing;
	}
	
	public double getySpacing() {
		return ySpacing;
	}
	
	public void setySpacing(double ySpacing) {
		this.ySpacing = ySpacing;
	}
	
	public float[] getElevationMatrix() {
		return elevationMatrix;
	}
	
	public void setElevationMatrix(float[] elevationMatrix) {
		this.elevationMatrix = elevationMatrix;
	}
			
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public boolean isLoaded() {
		synchronized (this) {
			return isLoaded;	
		}		
	}

	public void setLoaded(boolean isLoaded) {
		synchronized (this) {
			this.isLoaded = isLoaded;	
		}				
	}
	
	public float[] getTextureCoords() {
		return textureCoords;
	}
	
	public void setTextureCoords(float[] textureCords) {
		this.textureCoords = textureCords;
	}

	
	public void load(){
		
		int current_coln = 0;
		int current_row = 0;
		float[] vertices = new float[elevationMatrix.length * 3];
		int[] indices = new int[vertices.length * 3];
		float[] normals = new float[0];
		float[] colors = new float[0];
		
		double yMaxDim = (yDimension-1) * ySpacing;
		double xMaxDim = (xDimension-1) * xSpacing;
		double yCenter = yMaxDim / 2;
		double xCenter = xMaxDim / 2;
		//calculate vertices
		for(int i = 0; i < elevationMatrix.length; i++){
			current_coln = i % yDimension;
			current_row = (int) (i / xDimension);
			vertices[i*3] = (float) ((current_coln * xSpacing) - xCenter);
			vertices[(i*3)+1] = (float) ((yMaxDim - (current_row * ySpacing)) - yCenter);
			vertices[(i*3)+2] = (float) elevationMatrix[i];
		}		
		
		//calculate indices
		int i = 0;
		for(int y = 0; y < yDimension -1; y++) {
			for(int x = 0; x < xDimension -1; x++) {
				indices[i++] = y * xDimension + x;
				indices[i++] = (y + 1) * xDimension + x;
				indices[i++] = (y + 1) * xDimension + x + 1;
				
				indices[i++] = y * xDimension + x;
				indices[i++] = (y + 1) * xDimension + x + 1;				
				indices[i++] = y * xDimension + x + 1; 								
		  	}
		}
			
	    if(textureCoords == null || textureCoords.length == 0){
	    	computateTextureCords();	    	
	    } else {
	    	//flips rows
	    	// http://duriansoftware.com/joe/An-intro-to-modern-OpenGL.-Chapter-2.1:-Buffers-and-Textures.html
	    	// Texture sampling and texture parameters
	    	float[] textureCoorsTmp = new float[textureCoords.length];
	    	i = 0; //reset variable
	    	for(int j = yDimension -1; j >= 0 ; j--){
	    		for(int z = 0; z < xDimension; z++){
	    			textureCoorsTmp[i++] = textureCoords[((j*xDimension*2)+z*2)];
					textureCoorsTmp[i++] = textureCoords[((j*xDimension*2)+z*2+1)];	
	    		}
	    	}	    	  		    		   
	    	
	    	textureCoords = null;
	    	textureCoords = textureCoorsTmp; 
	    }
	    
	    this.setData(vertices, normals, textureCoords, colors, indices);
	    this.getGeometry().computeVertexNormals();
		
	}


	private void computateTextureCords() {
				
		int xdim = xDimension -1;
		int ydim = yDimension -1;		
		textureCoords = new float[xDimension * yDimension * 2];

		int vertex = 0;
		for(int y = 0; y < xDimension; y++){
			for(int x = 0; x < yDimension; x++){
//				RajLog.d("TEXTCOORDS: x="+x+" y="+y+" xDimension="+xDimension);
				textureCoords[vertex++] = ((float) x / (float) xdim);
				textureCoords[vertex++] = ((float) y /(float) ydim);
			}			
		}
		
	}		
	
}
