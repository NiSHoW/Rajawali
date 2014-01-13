package rajawali.terrain.virtual;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import android.os.AsyncTask;
import android.os.SystemClock;

import rajawali.materials.AResourceManager;
import rajawali.materials.textures.TextureManager;
import rajawali.parser.ParsingException;
import rajawali.parser.terrain.VirtualTerrainModelLoader;
import rajawali.renderer.AAsyncTask;
import rajawali.renderer.IAsyncTask;
import rajawali.renderer.IAsyncTaskListener;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;


/**
 * 
 * @author Nisho
 * you ned to set manually in render initilization
 * 			
 * VirtualTerrainManager vtm = VirtualTerrainManager.getInstance();
   vtm.setContext(getContext());
   vtm.registerRenderer(render);
 */
public class VirtualTerrainManager extends AResourceManager implements IAsyncTaskListener {

	private static VirtualTerrainManager instance = null;

	public static VirtualTerrainManager getInstance(){
		if(instance == null){
			instance = new VirtualTerrainManager();
		}
		return instance;
	}
	
	@Override
	public TYPE getFrameTaskType() {		
		return TYPE.OBJECT3D;
	}


	private double garbageDistanceMoltiplicator = 10;
	private List<IVirtualTerrainModel> onLoading;
	private Map<IVirtualTerrainModel, VirtualTerrainModelLoader> terrainModels;
	
	private VirtualTerrainManager() {
		terrainModels = Collections.synchronizedMap(new HashMap<IVirtualTerrainModel, VirtualTerrainModelLoader>());
		onLoading = Collections.synchronizedList(new ArrayList<IVirtualTerrainModel>());
		mRenderers = Collections.synchronizedList(new CopyOnWriteArrayList<RajawaliRenderer>());
	}

	/**
	 * Bind loading of this model
	 * @param model
	 * @param loader
	 */
	public void bindTerrainLoad(IVirtualTerrainModel model, VirtualTerrainModelLoader loader){
		terrainModels.put(model, loader);
	}
	
	/**
	 * Load model if is binded
	 * @param model
	 */
	public void load(IVirtualTerrainModel model){
		if(onLoading.contains(model)){
			return;	
		}
		if(terrainModels.containsKey(model)){
			VirtualTerrainModelLoader loader = terrainModels.get(model);
			onLoading.add(model);
			RajLog.d("LOAD ASYNC MODEL");
			VirtualTerrainModelAsyncLoaderTask task = new VirtualTerrainModelAsyncLoaderTask();
			task.addListener(this);
			task.addModelLoader(loader);
			mRenderer.addAsyncTasks(task);
			mRenderer.executeAsyncTasks();
		} else {
			RajLog.d("NO MODEL TO LOAD");
		}
	}
	
	/**
	 * Garbage a model much far
	 * @param model
	 * @param currentDistance
	 */
	public void garbageModels(IVirtualTerrainModel model, double currentDistance) {
		if(!terrainModels.containsKey(model)){
			return;
		}
		
		double distanceLimit = model.getDistance() * garbageDistanceMoltiplicator;
		if(distanceLimit < currentDistance){
			model.destroy();
			model.setLoaded(false);
		}
	}
	
	
	/**
	 * Async Task for loading models
	 * @author Nisho
	 */
	public class VirtualTerrainModelAsyncLoaderTask extends AAsyncTask {

		private List<VirtualTerrainModelLoader> loaders;
		private List<IVirtualTerrainModel> parsedModels;

		
		public VirtualTerrainModelAsyncLoaderTask() {
			super();
			loaders =  new ArrayList<VirtualTerrainModelLoader>();
			parsedModels =  new ArrayList<IVirtualTerrainModel>();
		}
		
		public void addModelLoader(VirtualTerrainModelLoader loader){
			loaders.add(loader);
		}		

		public List<IVirtualTerrainModel> getParsedModels(){
			return parsedModels;
		}
				
		public boolean doTask(RajawaliRenderer renderer) {
			for (VirtualTerrainModelLoader loader : loaders) {
				try {
					RajLog.d("Load Object");
					loader.parse();
					parsedModels.add(((IVirtualTerrainModel)loader.getParsedObject()));
					((IVirtualTerrainModel)loader.getParsedObject()).setLoaded(true);
					return true;		
				} catch (Exception e) {
					RajLog.e(""+e.getMessage());
				}				
			}			
			return false;
		}
		
	}
	

	public void onAsyncTaskProgressNotify(IAsyncTask task, Entry<Integer, String> lastValue) {
		RajLog.d("Change state:"+lastValue.getValue());
	}

	public void onAsyncTaskEnd(IAsyncTask task) {	
		List<IVirtualTerrainModel> models = ((VirtualTerrainModelAsyncLoaderTask) task).getParsedModels();
		RajLog.d("REmove task of loaded object");
		for(IVirtualTerrainModel model: models){			
			if(onLoading.contains(model))
				onLoading.remove(model);
		}	
	}
}
