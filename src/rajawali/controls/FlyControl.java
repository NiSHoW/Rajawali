package rajawali.controls;

import rajawali.controls.gestures.MoveGestureDetector;
import rajawali.controls.gestures.RotateGestureDetector;
import android.R.xml;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import rajawali.ATransformable3D;
import rajawali.Camera;
import rajawali.math.Matrix4;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.math.vector.Vector3.Axis;
import rajawali.util.RajLog;

/**
 * @author Nisho
 */
public class FlyControl implements IControl {
	
	/**
	 * Object to trasform based on gesture,
	 * Can be a camera or other object
	 */
	private ATransformable3D mObject;
	
	private final Vector3 mMoveVector;
	private final Vector3 mRotationVector;
	private final Quaternion quaternion;
	private final Matrix4 rotationMatrix;
	
	private double mMovementSpeed;
	private double mRollSpeed;
	
	private double mRollDelta;
	private double mScaleDelta;
	
	
	//traslate movement
	private double moveUp = 0;
	private double moveDown = 0; 
	private double moveLeft = 0; 
	private double moveRight = 0;
	
	//scale movements
	private double moveForward = 0; 
	private double moveBack = 0;
	
	//rotations movement
	private double movePitchUp = 0; 
	private double movePitchDown = 0; 
	private double moveYawLeft = 0; 
	private double moveYawRight = 0; 
	private double moveRollLeft = 0; 
	private double moveRollRight = 0;
	
	
	/**
	 * Gestures Detectors
	 */
	private ScaleGestureDetector mScaleDetector = null;
	private RotateGestureDetector mRotateDetector = null;
	private MoveGestureDetector mMoveDetector = null;
	
		
	public FlyControl(ATransformable3D object, Context context) {
		mObject = object;
		mMovementSpeed = 1000;
		mRollSpeed = Math.PI / 96;
		mMoveVector = new Vector3();
		mRotationVector = new Vector3();
		quaternion = new Quaternion();
		rotationMatrix = new Matrix4();
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		mRotateDetector = new RotateGestureDetector(context, new RotateListener());
		mMoveDetector = new MoveGestureDetector(context, new MoveListener());
	}
	
	/**
	 * OnTouch implementation
	 */
	public boolean onTouch(View v, MotionEvent event) {  
        
		mRotateDetector.onTouchEvent(event);
		//mScaleDetector.onTouchEvent(event);
		//mMoveDetector.onTouchEvent(event);
		updateMovements();
		return true;
	}
	
	

	public void update(double delta) {
		if(isChange()){
			
			double deltamove = (mObject.getZ() / mMovementSpeed) * (Math.log(1 + mObject.getZ()) / 2); 
			RajLog.d("ROTZ:"+(mRotationVector.z * mRollSpeed));
			
			Quaternion tmpQuaternion = new Quaternion(
	        	1,
	        	mRotationVector.x *  mRollSpeed, 
	        	mRotationVector.y * mRollSpeed, 
	        	mRotationVector.z * mRollSpeed 
	    	);		
			
			mObject.getOrientation(quaternion);
			quaternion.multiply(tmpQuaternion).normalize();	        
			rotationMatrix.setAll(quaternion.toRotationMatrix());
			
			if(mObject.isCamera()){
				RajLog.d("is camera");
				RajLog.d("VM:"+((Camera)mObject).getViewMatrix().toString());
				//((Camera)mObject).setUseRotationMatrix(true);
				//((Camera)mObject).setRotationMatrix(rotationMatrix);
			} 
			
			mObject.setOrientation(quaternion);
			//mObject.setRotation(rotationMatrix.getDoubleValues());			
			
//	        mObject.setRotation(
//	        	quaternion.getYaw(false), 
//	        	quaternion.getPitch(false), 
//	        	quaternion.getRoll(false)
//        	);
	        
	//        mObject.getPosition().add(new Vector3(
	//    		mMoveVector.x * deltamove,
	//    		mMoveVector.y * deltamove,
	//    		mMoveVector.z * deltamove
	//		));
	//        
			reset();
		}
	}	
	
	protected void reset(){
    	moveUp = 0;
    	moveDown = 0; 
    	moveLeft = 0; 
    	moveRight = 0; 
    	moveForward = 0; 
    	moveBack = 0;
    	movePitchUp = 0; 
    	movePitchDown = 0; 
    	moveYawLeft = 0; 
    	moveYawRight = 0; 
    	moveRollLeft = 0; 
    	moveRollRight = 0;
	}
		

	
	public boolean isChange(){
		return  moveUp != 0 ||
		    	moveDown != 0 || 
		    	moveLeft != 0 ||
		    	moveRight != 0 ||
    			movePitchUp != 0 ||
		    	movePitchDown != 0 || 
		    	moveYawLeft != 0 ||
		    	moveYawRight != 0 ||
		    	moveRollLeft != 0 ||
		    	moveRollRight != 0 ||
		    	moveForward != 0 || 
		    	moveBack != 0;	
	}
	
	
	protected void updateMovements() {
		
        mRotationVector.x = ( -movePitchDown + movePitchUp );
        mRotationVector.y = ( -moveYawRight  + moveYawLeft );
        mRotationVector.z = ( -moveRollRight + moveRollLeft );
        
        mMoveVector.x = ( -moveLeft + moveRight );
        mMoveVector.y = ( -moveDown + moveUp );
        mMoveVector.z = ( -moveForward + moveBack );
	} 
	
	
	/**
	 * ScaleDetector
	 */	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {					
		@Override
		public boolean onScale(ScaleGestureDetector detector) {			
			RajLog.d("SCALE:"+detector.getScaleFactor());
			if(detector.getScaleFactor() < 1){
				moveBack = 1;
			} else {
				moveForward = 1;
			}
			
			return true;
		}	
	}
	
	/**
	 * Ratate detector listener
	 */
	private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {		
		@Override
		public boolean onRotate(RotateGestureDetector detector) {
			RajLog.d("ROT:"+detector.getRotationDegreesDelta());
			if(detector.getRotationDegreesDelta() > 0.5){
				moveRollRight = 1;				
			} else if(detector.getRotationDegreesDelta() < 0.1){
				moveRollLeft = 1;
			}
			return true;
		}
	}	

	/**
	 * Move listener
	 */
	private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
		
		@Override
		public boolean onMove(MoveGestureDetector detector) {
			PointF f = detector.getFocusDelta();
			if(f.x > 1){				
				moveLeft = f.x;
			} else if(f.x < -1) {
				moveRight = -f.x;  
			}

			if(f.y > 1){
				moveUp = f.y;
			} else if(f.y < -1) {
				moveDown = -f.y;			
			}

			return true;
		}		
	}
	
	
	public void setRollSpeed(double movementSpeed){
		mMovementSpeed = movementSpeed;
	}
	
	public void setMovimentSpeed(double rollSpeed){
		mRollSpeed = rollSpeed;
	}
	

}
