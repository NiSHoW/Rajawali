package rajawali.parser.terrain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
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
import rajawali.terrain.virtual.VirtualTerrain;
import rajawali.util.RajLog;
import android.content.res.Resources;
import android.os.Environment;


/**
 * Parser for terrain file format created by me
 * A json file that rappresent a geospatial terrain
 * composed by segments (ElevationGrid)
 * 
 * file must be follow below definition:
 * 
//----------------------------------------------
// FILE STRUCT DEFINITION
//----------------------------------------------
// * field is required
{
	* type: 'terrain',
	name: 'custom_name',
	geoCordinate: [ latitude , longitude ],
	* segments: [{
		base: {url : ''} OR {resource_id: ''} OR {segment-definition: {} OR {serialized: data}},
		levels: [
			{url : ''} OR {resource_id: ''} OR {segment-definition: {}} OR {serialized: data}
			...
		]
	}],
	scale:'' //scala dell'terreno
}
 * 
 * @author Nicola Avancini (nicola.avancini@gmail.com)
 */
public class VirtualTerrainLoader implements IMeshLoader {

	//indicates when external segment if loading in background
	private boolean lazyLoad = true;
	
    protected final String TYPE = "type";
    protected final String NAME = "name";
    protected final String GEOCORDS = "geoCordinate";
    protected final String SEGMENTS = "segments";
    protected final String SCALE = "scale";
    protected final String ROTATION = "rotate";
    protected final String TRANSLATION = "translate";
	
	
	private VirtualTerrain mRootObject;
	
	private Resources mResources;
	private String mResourcePakage;
	private int resourceId;
	private String resourceName;
	
	private File mFile;
	
	private URL url;
	private String source;	
	
		
	public VirtualTerrainLoader() {
		mRootObject = new VirtualTerrain();
	}
	
	public VirtualTerrainLoader(File file) {
		this();
		this.mFile = file;
	}

	public VirtualTerrainLoader(Resources resources, int resourceId) {
		this();
		this.mResources = resources;
		this.resourceId = resourceId;
	}
	
	public VirtualTerrainLoader(Resources resources, String resourceName, String resourcePackage) {
		this();
		this.mResources = resources;
		this.mResourcePakage = resourcePackage;
		this.resourceName = resourceName;
	}
	
	public VirtualTerrainLoader(URL url) {
		this();
		this.url = url;		
	}
	
	public VirtualTerrainLoader(String source, boolean isSource){
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
			mResourcePakage = mResources.getResourcePackageName(resourceId);
			scan = new Scanner(new InputStreamReader(mResources.openRawResource(resourceId)));
			source = scan.next();
		} else if(source == null && resourceName != null){
			if(mResources == null){
				throw new ParseException("Cannot open resourceId file, Resources manager is wrong");
			}
			resourceId = mResources.getIdentifier(resourceName,"raw", mResourcePakage);			
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
	public VirtualTerrainLoader parse() throws ParsingException {
		
		//parse file
		if(source == null){
			loadSource();
			if(source == null)
				throw new ParseException("No source to read found.");
		} 				

		RajLog.d("source find:"+source);
		VirtualTerrainSegmentLoader vtsP = null;
		JSONTokener tokener = new JSONTokener(source);
		
		try {
			JSONObject jObj = (JSONObject) tokener.nextValue();
			//read type
			String type = jObj.getString(TYPE);
	    	if(!type.equals("terrain")){
	    		throw new ParsingException("File type is wrong.");
	    	}
	    	
	    	//------------------------------------------------
	    	//PARSE name
			//optional field
			if(jObj.has(NAME)){
				String name = jObj.getString(NAME);
				mRootObject.setName(name);
			}
			
			//-------------------------------------------------
			//PARSE CORDS
			if(jObj.has(GEOCORDS)){
				//optional field
				JSONArray cords = jObj.getJSONArray(GEOCORDS);
				//Not use for now
			}

			//-------------------------------------------------
			//PARSE SEGMENTS
			JSONArray segments = jObj.getJSONArray(SEGMENTS);			
			for(int i = 0; i < segments.length(); i++){
				RajLog.d("find a segment");
				JSONObject segment = segments.getJSONObject(i);
				vtsP = new VirtualTerrainSegmentLoader(segment.toString(), true);
				if(mResources != null)
					vtsP.setResources(mResources, mResourcePakage);
				vtsP.setLazyLoading(this.lazyLoad);
				vtsP.parse();
				mRootObject.addChild(vtsP.getParsedObject());
			}
			
	    	//------------------------------------------------
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

	public VirtualTerrain getParsedObject() {
		return mRootObject;
	}
	
}
