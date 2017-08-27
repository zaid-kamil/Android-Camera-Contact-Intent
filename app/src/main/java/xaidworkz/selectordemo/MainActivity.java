package xaidworkz.selectordemo;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import xaidworkz.selectordemo.utils.AlbumStorageDirFactory;
import xaidworkz.selectordemo.utils.BaseAlbumDirFactory;
import xaidworkz.selectordemo.utils.FroyoAlbumDirFactory;

import static android.provider.ContactsContract.Contacts;

public class MainActivity extends BaseActivity {


    public static final int REQUEST_EXTERNAL_STORAGE = 91;
    /*FOR CAMERA INTENT */
    private static final int ACTION_TAKE_PHOTO = 1;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final int REQUEST_SELECT_CONTACT = 23;
    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    private static final String TAG = "hahaha";
    Bitmap mImageBitmap;
    private String contactID;     // contacts unique ID
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    private String mCurrentPhotoPath;
    private TextView tvLauchSelection;
    private ImageView ivSelected;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    snack(ivSelected, "No magic here");
                    return true;
                case R.id.navigation_phone:
                    if (handlePermission(Manifest.permission.READ_CONTACTS, REQUEST_SELECT_CONTACT)) {
                        fetchContact();
                    }
                    return true;
                case R.id.navigation_notifications:
                    snack(ivSelected, "not here also");
                    return true;
            }
            return false;
        }

    };
    private File imagefile;

    /*contact fields*/
    private Uri contactUri;
    private ImageView ivPic;
    private TextView tvName;
    private TextView tvMobile;
    private CardView cvContact;
    /*contact fields ends*/

    private void fetchContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(Contacts.CONTENT_TYPE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_CONTACT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLauchSelection = (TextView) findViewById(R.id.tvLauchSelection);
        ivSelected = (ImageView) findViewById(R.id.ivSelection);

        /*contact display view*/

        cvContact = (CardView) findViewById(R.id.cvContact);
        ivPic = (ImageView) findViewById(R.id.ivPic);
        tvName = (TextView) findViewById(R.id.tvName);
        tvMobile = (TextView) findViewById(R.id.tvMobile);

        /*contact displat view end*/

        tvLauchSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initSelectionSetup();
            }
        });

        mImageBitmap = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void initSelectionSetup() {
        if (handlePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_EXTERNAL_STORAGE)) {
            dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    snack(ivSelected, "Permission Granted");
                    initSelectionSetup();
                }
            }
        }
        if (requestCode == REQUEST_SELECT_CONTACT) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    snack(ivSelected, "Permission Granted");
                    fetchContact();
                }
            }
        }
    }

    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        snack(ivSelected, " failed to create directory");
                        return null;
                    }
                }
            }

        }

        return storageDir;
    }

    private File setUpPhotoFile() throws IOException {

        imagefile = createImageFile();
        mCurrentPhotoPath = imagefile.getAbsolutePath();

        return imagefile;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private void setPic() {
        Picasso.with(context).load(Uri.fromFile(imagefile)).into(ivSelected);
        ivSelected.setVisibility(View.VISIBLE);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File f = null;

        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }
        startActivityForResult(takePictureIntent, actionCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            case REQUEST_SELECT_CONTACT:

                contactUri = data.getData();
                retrieveContactName();
                retrieveContactNumber();
                retrieveContactPhoto();
                cvContact.setVisibility(View.VISIBLE);
                break;

        } // switch
    }

    /*phone*/

    private void retrieveContactPhoto() {

        Bitmap photo = null;
        InputStream inpStream = null;
        try {
            inpStream = Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(Contacts.CONTENT_URI, Long.valueOf(contactID)));
            if (inpStream != null) {
                photo = BitmapFactory.decodeStream(inpStream);
                ivPic.setImageBitmap(photo);
            }
        } catch (Exception e) {
            snack(ivPic, e.getMessage());
        }
    }


    private void retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(contactUri, new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();
        if (contactNumber != null) {
            tvMobile.setText(contactNumber);
        }
    }

    private void retrieveContactName() {

        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();
        if (contactName != null) {
            tvName.setText(contactName);
        }


    }

    /*end phone*/

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            galleryAddPic();
            setPic();
            mCurrentPhotoPath = null;
        }

    }

    // Some lifecycle callbacks so that the image can survive orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
        outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
        ivSelected.setImageBitmap(mImageBitmap);
        ivSelected.setVisibility(savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? ImageView.VISIBLE : ImageView.INVISIBLE
        );

    }
}
