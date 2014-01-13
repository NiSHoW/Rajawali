package rajawali.renderer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import android.os.AsyncTask;

/**
 * Abstract implemntation of Async Task for render
 * @author Nicola Avancini (nicola.avancini@gmail.com)
 */
public abstract class AAsyncTask extends AsyncTask<RajawaliRenderer, Entry<Integer, String> , Boolean> implements IAsyncTask {

	public static int ERROR = -1;
	public static int INIT = 0;
	public static int COMPLETE = 100;
		
	protected RajawaliRenderer mRenderer;
	protected List<IAsyncTaskListener> mListeners;		
	
	public AAsyncTask(){
		mListeners =  Collections.synchronizedList(new CopyOnWriteArrayList<IAsyncTaskListener>());
	}
		
	
	@Override
	@SuppressWarnings("unchecked")
	protected Boolean doInBackground(RajawaliRenderer... params) {
		if(params.length > 0 && params[0] instanceof RajawaliRenderer){
			//set defaults scene
			mRenderer = params[0];			
			//init task	
			if(doTask(mRenderer)){
				return afterTask(mRenderer);
			}			
		}
		
		publishProgress(
			new AsyncSceneTaskProgressEntry(ERROR, "Nothing to do. Check the params or use custom execute method")
		);
		return false;
	}
	
	
	
	public boolean beforeTask(RajawaliRenderer renderer) {
		return true;
	}
	
	
	public boolean afterTask(RajawaliRenderer renderer) {
		return true;
	}
	
	public void cancel() {
		this.cancel(true);					
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void onPreExecute() {
		publishProgress(
			new AsyncSceneTaskProgressEntry(INIT, "AsyncSceneTask will be starting.")
		);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void onCancelled() {
		publishProgress(
			new AsyncSceneTaskProgressEntry(ERROR, "AsyncSceneTask stoped.")
		);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void onPostExecute(Boolean result) {				
		AsyncSceneTaskProgressEntry entry = null;
		if(result){
			entry = new AsyncSceneTaskProgressEntry(COMPLETE, "AsyncSceneTask has been finish.");			
		} else {
			entry = new AsyncSceneTaskProgressEntry(ERROR, "Somethings was wrong, sorry.");
		}
		
		publishProgress(entry);
		
		for(IAsyncTaskListener listener : mListeners){
			listener.onAsyncTaskEnd(this);
		}
	}
	
	@Override
	protected void onProgressUpdate(Entry<Integer, String>... values) {
		Entry<Integer, String> lastValue = values[values.length-1];
		for(IAsyncTaskListener listener : mListeners){
			listener.onAsyncTaskProgressNotify(this, lastValue);
		}
	}		
	
	
    /**
     * Adds the specified listener to the list of listeners. If it is already
     * registered, it is not added a second time.
     *
     * @param IAsyncTask
     *            the listener to add.
     */
    public void addListener(IAsyncTaskListener listener){
    	if(mListeners.contains(listener))
    		mListeners.add(listener);
    }


    /**
     * Returns the number of listeners registered to this {@code IAsyncTask}.
     *
     * @return the number of observers.
     */
    public int countListeners(){
    	return mListeners.size();
    }

    /**
     * Removes the specified listener from the list of listeners. Passing null
     * won't do anything.
     *
     * @param observer
     *            the observer to remove.
     */
    public void deleteListener(IAsyncTaskListener listener){
    	if(mListeners.contains(listener))
    		mListeners.remove(listener);
    }
    

    /**
     * Removes all listeners from the list of listeners.
     */
    public void deleteListeners(){
    	mListeners.removeAll(mListeners);
    }
	
	
    /**
     * Execute task
     */
    public AsyncTask<RajawaliRenderer, Entry<Integer, String>, Boolean> execute(RajawaliRenderer renderer){
    	return this.execute(new RajawaliRenderer[] {renderer});    	
    }
	
	
	/**
	 * Internal implementations of Entry for progress messages
	 * @author Nisho
	 */
	public class AsyncSceneTaskProgressEntry implements Map.Entry<Integer, String> {

		protected int progress;
		protected String message;
		
	    public AsyncSceneTaskProgressEntry(Integer key, String value) {
	        this.progress = key;
	        this.message = value;
	    }
		
		public Integer getKey() {
			return progress;
		}

		public String getValue() {
			return message;
		}

		public String setValue(String object) {
			String old = message;
			message = object;
			return old;
		}
		
	}
	
}
