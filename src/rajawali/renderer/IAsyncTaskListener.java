package rajawali.renderer;

import java.util.Map.Entry;

/**
 * Listener for IAsyncTask
 * @author Nicola Avancini (nicola.avancini@gmail.com)
 */
public interface IAsyncTaskListener {

	public void onAsyncTaskEnd(IAsyncTask task);
	
	public void onAsyncTaskProgressNotify(IAsyncTask task, Entry<Integer, String> lastValue);
	
}
