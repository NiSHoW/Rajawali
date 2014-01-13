package rajawali.parser.terrain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import rajawali.Object3D;
import rajawali.math.vector.Vector3;
import rajawali.parser.IMeshLoader;
import rajawali.parser.ParsingException;
import rajawali.terrain.virtual.IVirtualTerrainModel;
import rajawali.terrain.virtual.VirtualTerrainManager;
import rajawali.terrain.virtual.VirtualTerrainSegment;
import rajawali.util.RajLog;
import android.content.res.Resources;
import android.os.Environment;

/**
 * 
 * 
//----------------------------------------------
// definizione formato segmento
// VirtualTerrainSegment
//----------------------------------------------
{  
	url : '', //(optional)
	resource_id: int, //(optional)
	serialized_data: '', //(optional)
	//OR DEFINITION
	type: 'segment', //must be first	
	name: 'custom_name',	
	lods: {
		base: {url : ''} OR {resource_id: ''} OR {model-definition: {} OR {serialized: data}},
		levels: [
			{url : ''} OR {resource_id: ''} OR {model-definition: {}} OR {serialized: data}
			...
		]
	}
	
	scale:double //scale (optional)
	rotate:[] //matrix
	translate: [] //matrix
}

 * @author Nisho
 *
 */
public class VirtualTerrainSegmentLoader implements IMeshLoader {

	//indicates when external segment if loading in background
	private boolean lazyLoad = true;
		
    protected final String TYPE = "type";
    protected final String NAME = "name";
    protected final String LODS = "lods";
    protected final String LODS_BASE = "base";
    protected final String LODS_LEVELS = "levels";
    protected final String LODS_LEVELS_DISTANCE = "distance";
    protected final String URL = "url";	
    protected final String RESOURCEID = "resource_id";
    protected final String RESOURCENAME = "resource_name";   
    protected final String SERIALIZED = "serialized";    

    protected final String SCALE = "scale";
    protected final String ROTATION = "rotate";
    protected final String TRANSLATION = "translate";	

	
	private VirtualTerrainSegment mRootObject;
	
	private Resources mResources;
	private String mResourcesPackage;
	private int resourceId = 0;
	private String resourceName;
	
	private File mFile;
	
	private URL url;
	private String source;	
	
	private JSONArray levels;
		
	public VirtualTerrainSegmentLoader() {
		mRootObject = new VirtualTerrainSegment();
	}
	
	public VirtualTerrainSegmentLoader(File file) {
		this();
		this.mFile = file;
	}

	public VirtualTerrainSegmentLoader(Resources resources, int resourceId) {
		this();
		this.mResources = resources;
		this.resourceId = resourceId;
	}
	
	public VirtualTerrainSegmentLoader(Resources resources, String resourceName) {
		this();
		this.mResources = resources;
		this.resourceName = resourceName;
	}
	
	public VirtualTerrainSegmentLoader(URL url) {
		this();
		this.url = url;		
	}
	
	public VirtualTerrainSegmentLoader(String source, boolean isSource){
		this();
		if(isSource){
			this.source = source;
		} else {
			mFile = new File(Environment.getExternalStorageDirectory(), source);
		}	
	}
	
	/**
	 * return if parser delegate loading of segments to AsyncRendererTask
	 * @return
	 */
	public boolean isLazyLoading() {
		return lazyLoad;
	}
	
	/**
	 * Set delegation of segment loading to AsyncRendererTask
	 * @param lazy
	 */
	public void setLazyLoading(boolean lazy) {
		lazyLoad = lazy;
	}
	
	
	public boolean hasSource(){
		return source != null;
	}
	
	
	public void setResources(Resources resources, String resourcesPackage){
		mResources = resources;
		mResourcesPackage = resourcesPackage;
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
			scan = new Scanner(new InputStreamReader(mResources.openRawResource(resourceId)));
			source = scan.next();
		} else if(source == null && resourceName != null){
			if(mResources == null){
				throw new ParseException("Cannot open resourceId file, Resources manager is wrong");
			}
			resourceId = mResources.getIdentifier(resourceName,"raw", mResourcesPackage);			
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
	
	
	/**
	 * Parse file, url  or string source
	 * @return this class
	 * @throws ParsingException
	 */	
	public VirtualTerrainSegmentLoader parse() throws ParsingException {
	
		if(source == null){
			loadSource();
			if(source == null)
				throw new ParseException("No source to read found.");
		}
		
		RajLog.d("Parsing Segment:"+source);
		JSONTokener tokener = new JSONTokener(source);
		
		try {
			JSONObject jObj = (JSONObject) tokener.nextValue();
			//read type
			String type = jObj.getString(TYPE);
	    	if(!type.equals("segment")){
	    		throw new ParsingException("File type is wrong.");
	    	}	    	
	    	
	    	//------------------------------------------------
	    	//PARSE name
			//optional field
			if(jObj.has(NAME)){
				String name = jObj.getString(NAME);
				mRootObject.setName(name);
			}

	    	//------------------------------------------------
	    	//PARSE lods
			JSONObject lods = jObj.getJSONObject(LODS);
			if(lods.has(LODS_BASE)){			
				 RajLog.d("Find base element of "+mRootObject.getName());
				 JSONObject level = lods.getJSONObject(LODS_BASE);
				 String ltype = level.getString(TYPE);

				VirtualTerrainModelLoader vtmLoader = 
						new VirtualTerrainModelLoader(ltype, level.toString(), true);
				vtmLoader.setResouces(mResources, mResourcesPackage);
				vtmLoader.parse();						
				Object3D model = vtmLoader.getParsedObject();
				((IVirtualTerrainModel) model).setLoaded(true);
				mRootObject.addBaseLevel(model);
			}
			
			if(lods.has(LODS_LEVELS)){
				RajLog.d("Parse levels "+mRootObject.getName());
				levels = lods.getJSONArray(LODS_LEVELS);
				for (int i = 0; i < levels.length(); i++) {
					parseLevel(i);
				}
			}
			
	    	//------------------------------------------------
	    	//PARSE scale
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
		
		return this;
	}
	
	/**
	 * 
	 * @param i
	 * @throws ParsingException
	 */
	public void parseLevel(int i) throws ParsingException {
				
		try {
		
			JSONObject level = levels.getJSONObject(i);
			String ltype = level.getString(TYPE);
			double distance = level.getDouble(LODS_LEVELS_DISTANCE);

			 VirtualTerrainModelLoader vtmLoader = null;
			 //INITIALIZE MODEL LOADER
			 if(level.has(URL)){
				try {
					URL url = new URL(level.getString(URL));
					vtmLoader = new VirtualTerrainModelLoader(ltype, url);
				} catch (MalformedURLException e) {}
			 } else if(level.has(RESOURCEID)){
					int resourceId = level.getInt(RESOURCEID);
					vtmLoader = new VirtualTerrainModelLoader(ltype, mResources, resourceId);
			 } else if(level.has(RESOURCENAME)){
					String resourceName = level.getString(RESOURCENAME);
					vtmLoader = new VirtualTerrainModelLoader(ltype, mResources, resourceName, mResourcesPackage);
			 } else {
				 vtmLoader = new VirtualTerrainModelLoader(ltype, level.toString(), true);
				 vtmLoader.setResouces(mResources, mResourcesPackage);
			 }

			 if(vtmLoader != null){
				Object3D model = vtmLoader.getParsedObject();
					
				if(lazyLoad){
					VirtualTerrainManager.getInstance()
						.bindTerrainLoad(((IVirtualTerrainModel)model), vtmLoader);
				} else {
					vtmLoader.parse();
				}
				
				mRootObject.addLevel(model, distance);
			 } else {
				 RajLog.w("Skiped malformed model definition in "+mRootObject.getName());
			 }		
			
		} catch (JSONException e) {
			throw new ParseException(e.getMessage());
		}
	}

	
	public VirtualTerrainSegment getParsedObject() {
		return mRootObject;
	}	


}
