package com.lescomber.vestige.cgl;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class CGLSurfaceView extends GLSurfaceView {

	public CGLSurfaceView(Context context) {
		super(context);

		// Use Open GL ES 2.0
		setEGLContextClientVersion(2);

		// Use 32-bit depth to give our images
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);

		// Init renderer
		final CGLRenderer renderer = new CGLRenderer();
		setRenderer(renderer);
	}
}