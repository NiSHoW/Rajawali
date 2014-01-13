package rajawali.parser.terrain.virtual;

import java.lang.reflect.Field;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.Texture;
import rajawali.parser.ParsingException;
import rajawali.parser.terrain.IVirtualTerrainModelLoader;
import rajawali.terrain.TerrainMaterial;
import rajawali.terrain.virtual.IVirtualTerrainModel;
import rajawali.terrain.virtual.models.VirtualTerrainElevationGrid;
import rajawali.util.RajLog;
import android.content.res.Resources;
import android.graphics.Color;
import android.opengl.GLES20;

/**
 * 
//----------------------------------------------
//----------------------------------------------
{
	type: 'gridModel', //must be first	
	url: '',
	resource_id: int,
	serialized: '',
	//definition
	xDimension: int,
	yDimension: int,
	xSpacing: double,
	ySpacing: double,
	elevationMatrix: [(doubles values)...],
	textureCords: [(doubles values)...],
	textures: ['image-name'], //not support for multitexture
	translation: [] //translation matrix	refered to terrain local cordinates
}
 * 
 * @author Nisho
 *
 */
public class VirtualTerrainElevationGridLoader  implements IVirtualTerrainModelLoader {

    protected final String X_DIMENSION = "xDimension";
    protected final String Y_DIMENSION = "yDimension";
    protected final String X_SPACING = "xSpacing";
    protected final String Y_SPACING = "ySpacing";
    protected final String ELEVATION_MATRIX = "elevationMatrix";
    protected final String TEXTURE_COORDS = "textureCoords";

    
	private VirtualTerrainElevationGrid mRootObject;
	private String source;
	private boolean serialized;
	private Resources resources = null;
	private String resourcesPackage;
	
	public VirtualTerrainElevationGridLoader(IVirtualTerrainModel mRootObject) throws ParsingException {
		if(!(mRootObject instanceof VirtualTerrainElevationGrid)){
			throw new ParsingException("this object not extends VirtualTerrainElevationGrid");
		}
		
		this.mRootObject = (VirtualTerrainElevationGrid) mRootObject;
	}
	
	public VirtualTerrainElevationGridLoader(IVirtualTerrainModel mRootObject, String source) throws ParsingException {
		this(mRootObject);
		this.source = source;
	}

	public VirtualTerrainElevationGridLoader(IVirtualTerrainModel mRootObject, String source, boolean serialized) throws ParsingException {
		this(mRootObject, source);
		this.serialized = serialized;
	}
	
	public IVirtualTerrainModelLoader parse() throws ParsingException {
		if(source == null){
			throw new ParsingException("Set source before parsing");
		}
		
		//TODO serialization
		
		JSONTokener tokener = new JSONTokener(source);
		
		try {
			JSONObject jObj = (JSONObject) tokener.nextValue();
			//read type
			mRootObject.setxDimension(jObj.getInt(X_DIMENSION));
			mRootObject.setyDimension(jObj.getInt(Y_DIMENSION));
			mRootObject.setxSpacing(jObj.getInt(X_SPACING));
			mRootObject.setySpacing(jObj.getInt(Y_SPACING));
			
			int dimension = mRootObject.getxDimension() * mRootObject.getyDimension();
			float[] matrix = new float[dimension];
			JSONArray jmatrix = jObj.getJSONArray(ELEVATION_MATRIX);
			for (int i = 0; i < jmatrix.length(); i++) {
				matrix[i] = (float) jmatrix.getDouble(i);
			}
			
			mRootObject.setElevationMatrix(matrix);		
			
			if(jObj.has(TEXTURE_COORDS)){
				JSONArray jtextureCoords = jObj.getJSONArray(TEXTURE_COORDS);
				float[] textureCoords = new float[jtextureCoords.length()];
				for (int i = 0; i < jtextureCoords.length(); i++) {
					textureCoords[i] = (float) jtextureCoords.getDouble(i);
				}
				mRootObject.setTextureCoords(textureCoords);
			}			
			
		} catch (JSONException e) {
			throw new ParseException(e.getMessage());
		}
		
		return this;
	}

	public VirtualTerrainElevationGrid getParsedObject() {
		return this.mRootObject;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public void setSerialized(boolean serialized) {
		this.serialized = serialized;
	}

	public void setResouces(Resources resources, String resourcesPackage) {
		this.resources = resources;
		this.resourcesPackage = resourcesPackage;
	}
	
}
