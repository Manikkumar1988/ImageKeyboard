package challenge.mani.com.customkeyboard;

import android.app.AppOpsManager;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageKeyboard extends InputMethodService {
  private static final String TAG = "ImageKeyboard";
  private static final String MIME_TYPE_PNG = "image/png";
  private static final String AUTHORITY = "com.example.myapp.fileprovider";
  private File mGifFile;
  private Button mGifButton;


  private boolean isCommitContentSupported(
      @Nullable EditorInfo editorInfo, @NonNull String mimeType) {
    if (editorInfo == null) {
      return false;
    }

    final InputConnection ic = getCurrentInputConnection();
    if (ic == null) {
      return false;
    }

    if (!validatePackageName(editorInfo)) {
      return false;
    }

    final String[] supportedMimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo);
    for (String supportedMimeType : supportedMimeTypes) {
      if (ClipDescription.compareMimeTypes(mimeType, supportedMimeType)) {
        return true;
      }
    }
    return false;
  }


  private void doCommitContent(@NonNull String description, @NonNull String mimeType,
      @NonNull File file) {
    final EditorInfo editorInfo = getCurrentInputEditorInfo();

    // Validate packageName again just in case.
    if (!validatePackageName(editorInfo)) {
      return;
    }

    final Uri contentUri = FileProvider.getUriForFile(this, AUTHORITY, file);

    // As you as an IME author are most likely to have to implement your own content provider
    // to support CommitContent API, it is important to have a clear spec about what
    // applications are going to be allowed to access the content that your are going to share.
    final int flag;
    if (Build.VERSION.SDK_INT >= 25) {
      // On API 25 and later devices, as an analogy of Intent.FLAG_GRANT_READ_URI_PERMISSION,
      // you can specify InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION to give
      // a temporary read access to the recipient application without exporting your content
      // provider.
      flag = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
    } else {
      // On API 24 and prior devices, we cannot rely on
      // InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION. You as an IME author
      // need to decide what access control is needed (or not needed) for content URIs that
      // you are going to expose. This sample uses Context.grantUriPermission(), but you can
      // implement your own mechanism that satisfies your own requirements.
      flag = 0;
      try {
        // TODO: Use revokeUriPermission to revoke as needed.
        grantUriPermission(
            editorInfo.packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      } catch (Exception e){
        Log.e(TAG, "grantUriPermission failed packageName=" + editorInfo.packageName
            + " contentUri=" + contentUri, e);
      }
    }

    final InputContentInfoCompat inputContentInfoCompat = new InputContentInfoCompat(
        contentUri,
        new ClipDescription(description, new String[]{mimeType}),
        null /* linkUrl */);
    InputConnectionCompat.commitContent(
        getCurrentInputConnection(), getCurrentInputEditorInfo(), inputContentInfoCompat,
        flag, null);
  }


  private boolean validatePackageName(@Nullable EditorInfo editorInfo) {
    if (editorInfo == null) {
      return false;
    }
    final String packageName = editorInfo.packageName;
    if (packageName == null) {
      return false;
    }

    // In Android L MR-1 and prior devices, EditorInfo.packageName is not a reliable identifier
    // of the target application because:
    //   1. the system does not verify it [1]
    //   2. InputMethodManager.startInputInner() had filled EditorInfo.packageName with
    //      view.getContext().getPackageName() [2]
    // [1]: https://android.googlesource.com/platform/frameworks/base/+/a0f3ad1b5aabe04d9eb1df8bad34124b826ab641
    // [2]: https://android.googlesource.com/platform/frameworks/base/+/02df328f0cd12f2af87ca96ecf5819c8a3470dc8
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return true;
    }

    final InputBinding inputBinding = getCurrentInputBinding();
    if (inputBinding == null) {
      // Due to b.android.com/225029, it is possible that getCurrentInputBinding() returns
      // null even after onStartInputView() is called.
      // TODO: Come up with a way to work around this bug....
      Log.e(TAG, "inputBinding should not be null here. "
          + "You are likely to be hitting b.android.com/225029");
      return false;
    }
    final int packageUid = inputBinding.getUid();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      final AppOpsManager appOpsManager =
          (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
      try {
        appOpsManager.checkPackage(packageUid, packageName);
      } catch (Exception e) {
        return false;
      }
      return true;
    }

    final PackageManager packageManager = getPackageManager();
    final String possiblePackageNames[] = packageManager.getPackagesForUid(packageUid);
    for (final String possiblePackageName : possiblePackageNames) {
      if (packageName.equals(possiblePackageName)) {
        return true;
      }
    }
    return false;
  }



  private static File getFileForResource(
      @NonNull Context context, @RawRes int res, @NonNull File outputDir,
      @NonNull String filename) {
    final File outputFile = new File(outputDir, filename);
    final byte[] buffer = new byte[4096];
    InputStream resourceReader = null;
    try {
      try {
        resourceReader = context.getResources().openRawResource(res);
        OutputStream dataWriter = null;
        try {
          dataWriter = new FileOutputStream(outputFile);
          while (true) {
            final int numRead = resourceReader.read(buffer);
            if (numRead <= 0) {
              break;
            }
            dataWriter.write(buffer, 0, numRead);
          }
          return outputFile;
        } finally {
          if (dataWriter != null) {
            dataWriter.flush();
            dataWriter.close();
          }
        }
      } finally {
        if (resourceReader != null) {
          resourceReader.close();
        }
      }
    } catch (IOException e) {
      return null;
    }
  }


  @Override
  public void onCreate() {
    super.onCreate();

    // TODO: Avoid file I/O in the main thread.
    final File imagesDir = new File(getFilesDir(), "images");
    imagesDir.mkdirs();
    mGifFile = getFileForResource(this, R.raw.diwali_one, imagesDir, "image.png");
  }

  @Override
  public View onCreateInputView() {
   /* mGifButton = new Button(this);
    mGifButton.setText("Insert PNG");
    mGifButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        ImageKeyboard.this.doCommitContent("A waving flag", MIME_TYPE_PNG, mGifFile);
      }
    });

    final LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.addView(mGifButton);

    return layout;*/

    View rootView = View.inflate(getApplicationContext(), R.layout.fragment_main,  null);


      int[] imageId = new int[] {
          R.drawable.gm_one, R.drawable.good_morn_two
      };


    CustomGrid adapter = new CustomGrid(getApplicationContext(), imageId);
    GridView grid=(GridView)rootView.findViewById(R.id.simpleGridView);
    grid.setAdapter(adapter);
    grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view,
          int position, long id) {
        ImageKeyboard.this.doCommitContent("A waving flag", MIME_TYPE_PNG, mGifFile);
      }
    });

    return rootView;
  }
}
