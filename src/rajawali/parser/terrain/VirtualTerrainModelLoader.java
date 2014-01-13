package rajawali.parser.terrain;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import rajawali.Object3D;
import rajawali.materials.textures.Texture;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.math.vector.Vector3;
import rajawali.parser.ILoader;
import rajawali.parser.IMeshLoader;
import rajawali.parser.ParsingException;
import rajawali.parser.terrain.virtual.VirtualTerrainElevationGridLoader;
import rajawali.terrain.TerrainMaterial;
import rajawali.terrain.virtual.IVirtualTerrainModel;
import rajawali.terrain.virtual.models.VirtualTerrainElevationGrid;
import rajawali.util.RajLog;
import android.content.res.Resources;
import android.graphics.Color;
import android.opengl.GLES20;
import android.os.Environment;



public class VirtualTerrainModelLoader implements IMeshLoader{

    protected final String ELEVATIONGRID = "elevationGrid";
    
    protected final String TEXTURES = "textures";
    protected final String SCALE = "scale";
    protected final String ROTATION = "rotate";
    protected final String TRANSLATION = "translate";
    
	private Object3D mRootObject;
	
	private String type;
	private IVirtualTerrainModelLoader loader;
	
	private Resources mResources;
	private String mResourcesPackage;
	
	private int resourceId = 0;
	private String resourceName;
	
	private File mFile;	
	private URL url;
	private String source;	

		
	public VirtualTerrainModelLoader(String type) throws ParsingException {
		this.type = type;
		initialize();
	}
	
	private void initialize() throws ParsingException {
		if(type.equals(ELEVATIONGRID)){
			mRootObject = new VirtualTerrainElevationGrid();
			loader = new VirtualTerrainElevationGridLoader(((IVirtualTerrainModel) mRootObject));
		} else {
			throw new ParsingException("Model type not found");
		}
	}

	public VirtualTerrainModelLoader(String type, File file) throws ParsingException {
		this(type);
		this.mFile = file;
	}

	public VirtualTerrainModelLoader(String type, Resources resources, int resourceId) throws ParsingException {
		this(type);
		this.mResources = resources;
		this.resourceId = resourceId;
	}
	
	public VirtualTerrainModelLoader(String type, Resources resources, String resourceName,  String resourcePackage) throws ParsingException {
		this(type);
		this.mResources = resources;
		this.mResourcesPackage = resourcePackage;
		this.resourceName = resourceName;
	}
	
	public VirtualTerrainModelLoader(String type, URL url) throws ParsingException {
		this(type);
		this.url = url;		
	}
	
	public VirtualTerrainModelLoader(String type, String source, boolean isSource) throws ParsingException {
		this(type);
		if(isSource){
			this.source = source;
		} else {
			mFile = new File(Environment.getExternalStorageDirectory(), source);
		}	
	}
	
	
	protected void loadSource() throws ParsingException {
		//create new Virtual terrain object	
		Scanner scan = null;
		
		//parse file
		if(source == null && url != null){
			//read file from remote
			try {
				scan = new Scanner(new InputStreamReader(url.openStream()));
			    source = scan.next();			    
			} catch (IOException e) {
				throw new ParseException(e.getMessage());
			} 
			
		} else if(source == null && resourceId > 0){
			if(mResources == null){
				throw new ParseException("Cannot open resourceId file, Resources manager is wrong");
			}
			mResourcesPackage = mResources.getResourcePackageName(resourceId);
			scan = new Scanner(new InputStreamReader(mResources.openRawResource(resourceId)));
			source = scan.next();
		} else if(source == null && resourceName != null){
			if(mResources == null){
				throw new ParseException("Cannot open resourceId file, Resources manager is wrong");
			}
			resourceId = mResources.getIdentifier(resourceName,"raw", mResourcesPackage);
			if(resourceId == 0){
				throw new ParseException("Cannot open resourceId file, resource can't be found");
			}
			scan = new Scanner(new InputStreamReader(mResources.openRawResource(resourceId)));
			source = scan.next();
		} else if(source == null && mFile != null){
			
			try {
				scan = new Scanner(mFile);
				source = scan.next();
			} catch (FileNotFoundException e) {
				throw new ParseException(e.getMessage());
			}
		} 
	}
	
	
	public ILoader parse() throws ParsingException {
		if(source == null){
			loadSource();
			if(source == null)
				throw new ParseException("No source to read found.");
		}
		
		RajLog.d("Parsing model source: "+source);		
		loader.setSource(source);
		if(mResources != null){
			loader.setResouces(mResources, mResourcesPackage);
		} else {
			RajLog.d("Resources skipped for null value");
		}
		loader.parse();		

		//retrive custom parsed object
		mRootObject = (Object3D) loader.getParsedObject();				
		
		TerrainMaterial material = new TerrainMaterial();
		material.setColorInfluence(0);
		mRootObject.setMaterial(material);
		
		JSONTokener tokener = new JSONTokener(source);
		
		try {
			JSONObject jObj = (JSONObject) tokener.nextValue();
			
			//PARSE TEXTURES
			if(jObj.has(TEXTURES)){
				JSONArray textures = jObj.getJSONArray(TEXTURES);
				for(int i = 0; i < textures.length(); i++){					
					//TODO check for url else
					if(mResources != null){
						String filename = textures.getString(i);
						int end = filename.lastIndexOf('.');
						if(end == -1) end = filename.length();
						filename = filename.substring(0, end).toLowerCase();						
						int resid = mResources.getIdentifier(filename, "drawable", mResourcesPackage);
						if(resid > 0){
							try {
								material.addTexture(new Texture(filename, resid));
							} catch (TextureException e) {
								throw new ParseException(e.getMessage());
							}
						} else {
							RajLog.w("Skip image '"+filename+"' because not founded in "+mResourcesPackage);							
						}
					} else {
						throw new ParseException("Set resources for load internal image");
					}
				}
			}		
			
			//PARSE TRANSLATION
			if(jObj.has(TRANSLATION)){
				Vector3 translation = new Vector3();
				JSONArray jtranslation = jObj.getJSONArray(TRANSLATION);
				translation.x = jtranslation.getDouble(0);
				translation.y = jtranslation.getDouble(1);
				translation.z = jtranslation.getDouble(2);
				mRootObject.setPosition(translation);
			}
			
			//PARSE SCALE
			if(jObj.has(SCALE)){
				Vector3 scale = new Vector3();
				JSONArray jscale = jObj.getJSONArray(SCALE);
				scale.x = jscale.getDouble(0);
				scale.y = jscale.getDouble(1);
				scale.z = jscale.getDouble(2);
				mRootObject.setScale(scale);
			}
			
			//PARSE ROTATION
			if(jObj.has(ROTATION)){
				Vector3 rotation = new Vector3();
				JSONArray jrotation = jObj.getJSONArray(ROTATION);
				rotation.x = jrotation.getDouble(0);
				rotation.y = jrotation.getDouble(1);
				rotation.z = jrotation.getDouble(2);
				mRootObject.setRotation(rotation);
			}			
			
			
		} catch (JSONException e) {
			throw new ParseException(e.getMessage());
		}			
		
		if(material.getTextureList().size() == 0){
			RajLog.d("Object has no textures, set default color");
			material.setColor(Color.WHITE);
			material.setColorInfluence(0.5f);
			material.enableLighting(false);
			mRootObject.setColor(Color.WHITE);
		}
		
		((IVirtualTerrainModel) mRootObject).load();
		
		return this;
	}

	public Object3D getParsedObject() {
		return mRootObject;
	}

	public void setResouces(Resources resources, String resourcesPackage) {
		this.mResources = resources;
		this.mResourcesPackage = resourcesPackage;
	}
}
