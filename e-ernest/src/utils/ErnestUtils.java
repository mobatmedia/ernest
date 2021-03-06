package utils;

import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Point3f;

/**
 * Miscellaneous utilities for Ernest.
 * @author Olivier
 */
public final class ErnestUtils 
{
	/**
	 * Returns the polar angle of this vector.
	 * @param vector A vector.
	 * @return The polar angle of this vector.
	 */
	public static float polarAngle(Vector3f vector) 
	{
		if (vector.y == 0)
			return 0;
		else
			return (float)Math.atan2((double)vector.y, (double)vector.x);
	}
	
	/**
	 * Returns the angle of rotation of a transformation.
	 * @param transform The transformation.
	 * @return The angle of rotation.
	 */
	public static float angle(Transform3D transform) 
	{
		Matrix3d mat = new Matrix3d();
		transform.get(mat);
		if (mat.m10 == 0)
			return 0;
		else
			return (float)Math.atan2(mat.m10, mat.m00);
	}
	
	/**
	 * Returns the polar angle of this vector.
	 * @param point A vector.
	 * @return The polar angle of this vector.
	 */
	public static float polarAngle(Point3f point) 
	{
		if (point.y == 0){
			if (point.x >= 0)
				return 0;
			else
				return (float)Math.PI;
		}
		else
			return (float)Math.atan2((double)point.y, (double)point.x);
	}
	
	/**
	 * Rotate a vector around the z axis.
	 * @param vector A vector.
	 * @param angle The angle of rotation
	 */
	public static void rotate(Vector3f vector, float angle) 
	{
		//Matrix3f rot = new Matrix3f();
		//rot.rotZ(angle);		
		//rot.transform(vector);
		
		Transform3D tf = new Transform3D();
		tf.rotZ(angle);
		tf.transform(vector);
		
	}	

	
	/**
	 * @param value rgb integer value.
	 * @return The hexadecimal code.
	 */
	public static String hexColor(int value) 
	{
		int r = (value & 0xFF0000)/65536;
		int g = (value & 0x00FF00)/256;
		int b = (value & 0x0000FF);
		
		String s = formatHex(r) + formatHex(g) + formatHex(b);

		return s;
	}
	
	public static String formatHex(int i)
	{
		if (i == 0)
			return "00";
		else if (i < 16)
			return "0" + Integer.toString(i, 16).toUpperCase();
		else
			return Integer.toString(i, 16).toUpperCase();
	}
	
	public static String format(float f, int d)
	{
		String format;
		//float exp = 10^d;
		format = Integer.toString(Math.round(f * (float)Math.pow(10, d)));
		if (d> 0) 
		{
			if (format.length() > d)
				format = format.substring(0, format.length()-d) + "." + format.substring(format.length()-d, format.length());
			if (format.length() == d)
				format = "0." + format;
			if (format.length() == d -1)
				format = "0.0" + format;
			// TODO manage d > 2
		}
		
		return format;
	}
	
	/**
	 * @param t The duration of sleep in 1/100 seconds
	 */
	public static void sleep(int t)
	{
		try
		{ 
			Thread.currentThread().sleep(t);
		}
		catch(InterruptedException ie)
		{}
	}

	public static float translationX(Transform3D transformation) {
		Vector3f vec = new Vector3f();
		transformation.get(vec);
		return vec.x;
	}

	public static float translationY(Transform3D transformation) {
		Vector3f vec = new Vector3f();
		transformation.get(vec);
		return vec.y;
	}
}
