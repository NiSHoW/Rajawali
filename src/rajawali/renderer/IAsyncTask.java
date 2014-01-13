package rajawali.renderer;

import java.util.List;
import java.util.Map.Entry;

import rajawali.terrain.virtual.IVirtualTerrainModel;
import android.os.AsyncTask;


/**
 * Interface for AsycTask used in renderer
 * can be implemented for doing operation in background as async object loading 
 * @author Nicola Avancini (nicola.avancini@gmail.com)
 */
public interface IAsyncTask {

	/**
	 * 
	 * @return
	 */
	public boolean beforeTask(RajawaliRenderer renderer);
	
	/**
	 * 
	 * @return
	 */
	public boolean doTask(RajawaliRenderer renderer);
	
	
	/**
	 * 
	 * @return
	 */
	public boolean afterTask(RajawaliRenderer renderer);
	
	
    /**
     * Adds the specified listener to the list of listeners. If it is already
     * registered, it is not added a second time.
     *
     * @param IAsyncTask
     *            the listener to add.
     */
    public void addListener(IAsyncTaskListener listener);


    /**
     * Returns the number of listeners registered to this {@code IAsyncTask}.
     *
     * @return the number of observers.
     */
    public int countListeners();

    /**
     * Removes the specified listener from the list of listeners. Passing null
     * won't do anything.
     *
     * @param observer
     *            the observer to remove.
     */
    public void deleteListener(IAsyncTaskListener listener);
    

    /**
     * Removes all listeners from the list of listeners.
     */
    public void deleteListeners();

    
    /**
     * Execute task
     */
    public AsyncTask<RajawaliRenderer, Entry<Integer, String>, Boolean> execute(RajawaliRenderer renderer);

    /**
     * cancel current task
     */
	public void cancel();

}
