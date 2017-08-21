package xaidworkz.selectordemo.utils;

import java.io.File;

import android.os.Environment;

public final class FroyoAlbumDirFactory extends AlbumStorageDirFactory {

	@Override
	public File getAlbumStorageDir(String albumName) {
		return new File(
		  Environment.getExternalStoragePublicDirectory(
		    Environment.DIRECTORY_PICTURES
		  ), 
		  albumName
		);
	}
}
