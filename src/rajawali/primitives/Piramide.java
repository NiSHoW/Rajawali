package rajawali.primitives;

import rajawali.Object3D;

/**
 * 
 * @author Nisho
 *
 */
public class Piramide extends Object3D {

	private float mSize;
	private boolean mCreateTextureCoords;
	private boolean mCreateVertexColorBuffer;

	/**
	 * Creates a cube primitive. Calling this constructor will create texture coordinates but no vertex color buffer. 
	 * @param size		The size of the cube.
	 */
	public Piramide(float size) {
		this(size, false, false, true, false);
	}

	/**
	 * Creates a cube primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 * 
	 * @param size			The size of the cube.
	 * @param isSkybox		A boolean that indicates whether this is a skybox or not. If set to true the normals will 
	 * 						be inverted.
	 */
	public Piramide(float size, boolean isSkybox) {
		this(size, isSkybox, true, true, false);
	}
	
	/**
	 * Creates a cube primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 * 
	 * @param size					The size of the cube.
	 * @param isSkybox				A boolean that indicates whether this is a skybox or not. If set to true the normals will 
	 * 								be inverted.
	 * @param hasCubemapTexture		A boolean that indicates a cube map texture will be used (6 textures) or a regular 
	 * 								single texture.
	 */
	public Piramide(float size, boolean isSkybox, boolean hasCubemapTexture)
	{
		this(size, isSkybox, hasCubemapTexture, true, false);
	}

	/**
	 * Creates a cube primitive.
	 * 
	 * @param size						The size of the cube.
	 * @param isSkybox					A boolean that indicates whether this is a skybox or not. If set to true the normals will 
	 * 									be inverted.
	 * @param hasCubemapTexture			A boolean that indicates a cube map texture will be used (6 textures) or a regular 
	 * 									single texture.
	 * @param createTextureCoordinates	A boolean that indicates whether the texture coordinates should be calculated or not.
	 * @param createVertexColorBuffer	A boolean that indicates whether a vertex color buffer should be created or not.
	 */
	public Piramide(float size, boolean isSkybox, boolean hasCubemapTexture, boolean createTextureCoordinates,
			boolean createVertexColorBuffer) {
		super();
		mSize = size;
		mHasCubemapTexture = hasCubemapTexture;
		mCreateTextureCoords = createTextureCoordinates;
		mCreateVertexColorBuffer = createVertexColorBuffer;
		init();
	}

	private void init()
	{
		float halfSize = mSize * .5f;
		float[] vertices = {
				// -- back
				halfSize, halfSize, halfSize, 			-halfSize, halfSize, halfSize,
				-halfSize, -halfSize, halfSize,			halfSize, -halfSize, halfSize, // 0-1-halfSize-3 front
				
				halfSize, halfSize, halfSize, 			halfSize, -halfSize, halfSize, 
				halfSize, -halfSize, -halfSize, 		halfSize, halfSize, -halfSize,// 0-3-4-5 right
				// -- front
				halfSize, -halfSize, -halfSize, 		-halfSize, -halfSize, -halfSize, 
				-halfSize, halfSize, -halfSize,			halfSize, halfSize, -halfSize,// 4-7-6-5 back
				
				-halfSize, halfSize, halfSize, 			-halfSize, halfSize, -halfSize, 
				-halfSize, -halfSize, -halfSize,		-halfSize,	-halfSize, halfSize,// 1-6-7-halfSize left
				
				halfSize, halfSize, halfSize, 			halfSize, halfSize, -halfSize, 
				-halfSize, halfSize, -halfSize, 		-halfSize, halfSize, halfSize, // top
				
				halfSize, -halfSize, halfSize, 			-halfSize, -halfSize, halfSize, 
				-halfSize, -halfSize, -halfSize,		halfSize, -halfSize, -halfSize,// bottom
		};

		float[] textureCoords = null;

		if (mCreateTextureCoords && !mHasCubemapTexture)
		{
			textureCoords = new float[]
			{
					0, 1, 1, 1, 1, 0, 0, 0, // front
					0, 1, 1, 1, 1, 0, 0, 0, // up
					0, 1, 1, 1, 1, 0, 0, 0, // back
					0, 1, 1, 1, 1, 0, 0, 0, // down
					0, 1, 1, 1, 1, 0, 0, 0, // right
					0, 1, 1, 1, 1, 0, 0, 0, // left
			};
		}

		float[] colors = null;
		if (mCreateVertexColorBuffer)
		{
			colors = new float[] {
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
			};
		}

		float n = 1;

		float[] normals = {
				0, 0, n, 0, 0, n, 0, 0, n, 0, 0, n, // front
				n, 0, 0, n, 0, 0, n, 0, 0, n, 0, 0, // right
				0, 0, -n, 0, 0, -n, 0, 0, -n, 0, 0, -n, // back
				-n, 0, 0, -n, 0, 0, -n, 0, 0, -n, 0, 0, // left
				0, n, 0, 0, n, 0, 0, n, 0, 0, n, 0, // top
				0, -n, 0, 0, -n, 0, 0, -n, 0, 0, -n, 0, // bottom
		};

		int[] indices = {
				0, 1, 2, 0, 2, 3,
				4, 5, 6, 4, 6, 7,
				8, 9, 10, 8, 10, 11,
				12, 13, 14, 12, 14, 15,
				16, 17, 18, 16, 18, 19,
				20, 21, 22, 20, 22, 23,
		};

		setData(vertices, normals, textureCoords, colors, indices);
		
		vertices = null;
		normals = null;
		textureCoords = null;
		colors = null;
		indices = null;
	}
	
}