package com.kbeanie.webviewuploadfile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.FileUtils;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.kbeanie.imagechooser.api.utils.ImageChooserBuilder;

import java.io.File;

public class MainActivity extends AppCompatActivity implements ImageChooserListener {

    private ImageChooserManager imageChooserManager;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessageArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebChromeClient(client);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://10.0.1.27:3000/input.html");
    }

    private int chooserType = -1;
    private String chooserPath = null;

    private void pickFile(String type) {
        ImageChooserBuilder builder = new ImageChooserBuilder(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                imageChooserManager = new ImageChooserManager(MainActivity.this, i, FileUtils.getDirectory("SS"));
                imageChooserManager.setImageChooserListener(MainActivity.this);
                try {
                    chooserType = i;
                    chooserPath = imageChooserManager.choose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onImageChosen(final ChosenImage chosenImage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("CI:", chosenImage.getFilePathOriginal());
                if (null == mUploadMessage && mUploadMessageArray == null) return;
                Uri result = Uri.fromFile(new File(chosenImage.getFileThumbnail()));
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                } else if (mUploadMessageArray != null) {
                    Uri[] uris = new Uri[1];
                    uris[0] = result;
                    mUploadMessageArray.onReceiveValue(uris);
                }
            }
        });
    }

    @Override
    public void onError(String s) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChooserType.REQUEST_CAPTURE_PICTURE || requestCode == ChooserType.REQUEST_PICK_PICTURE) {
            if (resultCode == RESULT_OK) {
                if (imageChooserManager == null) {
                    imageChooserManager = new ImageChooserManager(this, requestCode, FileUtils.getDirectory("TT"));
                    imageChooserManager.setImageChooserListener(this);
                    imageChooserManager.reinitialize(chooserPath);
                }
                imageChooserManager.submit(requestCode, data);
            }
        }
    }

    private WebChromeClient client = new WebChromeClient() {
        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
            mUploadMessage = uploadMsg;
            pickFile(AcceptType);
        }

        @SuppressWarnings("unused")

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
            mUploadMessage = uploadMsg;
            pickFile(AcceptType);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            pickFile("image/*");

        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, FileChooserParams fileChooserParams) {
            mUploadMessageArray = uploadMsg;
            pickFile("image/*");
            return true;
        }
    };
}
