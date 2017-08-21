package xaidworkz.selectordemo.utils;

import java.io.File;

import android.os.Environment;

public final class BaseAlbumDirFactory extends AlbumStorageDirFactory {

	// storage location for camera files
	private static final String CAMERA_DIR = "/xaidworkz/";

	@Override
	public File getAlbumStorageDir(String albumName) {
		return new File (
				Environment.getExternalStorageDirectory()
				+ CAMERA_DIR
				+ albumName
		);
	}
}
