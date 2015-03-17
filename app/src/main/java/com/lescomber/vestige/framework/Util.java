package com.lescomber.vestige.framework;

import java.util.Comparator;
import java.util.Random;

public class Util
{
	private static final float epsilon = 0.001f;

	public static final Random rand = new Random();        // Random number generator

	public static boolean equals(float f1, float f2)
	{
		return Math.abs(f1 - f2) < epsilon;
	}

	public static boolean equals(double d1, double d2)
	{
		return Math.abs(d1 - d2) < epsilon;
	}

	public static Comparator<Float> floatComparator = new Comparator<Float>()
	{
		@Override
		public int compare(Float float1, Float float2)
		{
			final int ret;
			final float dif = float1 - float2;
			if (dif < 0)
				ret = Math.min(Math.round(dif), -1);
			else if (dif == 0)
				ret = 0;
			else
				ret = Math.max(Math.round(dif), 1);

			return ret;
		}
	};
}
