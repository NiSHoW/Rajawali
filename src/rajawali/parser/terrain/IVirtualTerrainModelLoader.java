package rajawali.parser.terrain;

import android.content.res.Resources;
import rajawali.Object3D;
import rajawali.parser.ParsingException;


public interface IVirtualTerrainModelLoader {

	public IVirtualTerrainModelLoader parse() throws ParsingException;

	public void setResouces(Resources resources, String resourcesPackage);
	
	public Object3D getParsedObject();

	public void setSource(String source);
	
}
