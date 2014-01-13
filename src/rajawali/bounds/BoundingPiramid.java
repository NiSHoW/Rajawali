package rajawali.bounds;

import java.util.concurrent.atomic.AtomicInteger;

import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.Object3D;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Cube;


public class BoundingPiramid implements IBoundingVolume{

	protected Geometry3D mGeometry;
	protected final Vector3 mMin, mTransformedMin;
	protected final Vector3 mMax, mTransformedMax;
	protected final Vector3 mTmpMin, mTmpMax;
	protected final Vector3[] mPoints;
	protected final Vector3[] mTmp;
	protected int mI;
	protected Cube mVisualBox;
	protected final Matrix4 mTmpMatrix = new Matrix4(); //Assumed to never leave identity state
	protected AtomicInteger mBoundingColor = new AtomicInteger(0xffffff00);
	
	
	public BoundingPiramid() {
		mTransformedMin = new Vector3();
		mTransformedMax = new Vector3();
		mTmpMin = new Vector3();
		mTmpMax = new Vector3();
		mPoints = new Vector3[8];
		mTmp = new Vector3[8];
		mMin = new Vector3();
		mMax = new Vector3();
		for(int i=0; i<8; ++i) {
			mPoints[i] = new Vector3();
			mTmp[i] = new Vector3();
		}
	}
	
	public BoundingPiramid(Geometry3D geometry) {
		this();
		mGeometry = geometry;
		//calculateBounds(mGeometry);
		mMin.setAll(geometry.getMinLimit());
		mMax.setAll(geometry.getMaxLimit());
		//calculatePoints();
	}
	
	@Deprecated
	public void calculateBounds(Geometry3D geometry) {
		// TODO Auto-generated method stub
		
	}

	public void drawBoundingVolume(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Matrix4 mMatrix) {
		// TODO Auto-generated method stub
		
	}

	public void transform(Matrix4 matrix) {
		// TODO Auto-generated method stub
		
	}

	public boolean intersectsWith(IBoundingVolume boundingVolume) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object3D getVisual() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setBoundingColor(int color) {
		// TODO Auto-generated method stub
		
	}

	public int getBoundingColor() {
		// TODO Auto-generated method stub
		return 0;
	}

}
