package com.lescomber.vestige.cgl;

import android.opengl.GLES20;

public class CGLUtil {
	/**
	 * Creates a program, given source code for vertex and fragment shaders.
	 *
	 * @param vertexShaderCode   Source code for vertex shader.
	 * @param fragmentShaderCode Source code for fragment shader.
	 * @return Handle to program.
	 */
	public static int createProgram(String vertexShaderCode, String fragmentShaderCode) {
		// Load vertex shader
		final int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		GLES20.glShaderSource(vertexShader, vertexShaderCode);
		GLES20.glCompileShader(vertexShader);

		// Load fragment shader
		final int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
		GLES20.glCompileShader(fragmentShader);

		// Build the program.
		final int programHandle = GLES20.glCreateProgram();
		GLES20.glAttachShader(programHandle, vertexShader);
		GLES20.glAttachShader(programHandle, fragmentShader);
		GLES20.glLinkProgram(programHandle);

		// Flag shaders for deletion. They will not actually be deleted until their parent program is deleted
		GLES20.glDeleteShader(vertexShader);
		GLES20.glDeleteShader(fragmentShader);

		return programHandle;
	}
}