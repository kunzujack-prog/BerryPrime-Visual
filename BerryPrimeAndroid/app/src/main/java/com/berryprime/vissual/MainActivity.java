package com.berryprime.vissual;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.webkit.*;
import android.widget.Toast;
import android.util.Base64;
import androidx.webkit.WebViewAssetLoader;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private static final int PICK_FILE = 701;
    private ValueCallback<Uri[]> fileCallback;
    private WebView web;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF171B4B);
        getWindow().setNavigationBarColor(0xFF171B4B);
        web = new WebView(this);
        setContentView(web);
        WebView.setWebContentsDebuggingEnabled(false);
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true); s.setAllowContentAccess(true); s.setSupportMultipleWindows(false);
        s.setJavaScriptCanOpenWindowsAutomatically(true); s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.addJavascriptInterface(new AndroidBridge(), "BerryAndroid");
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView v, android.webkit.WebResourceRequest r) { return loader.shouldInterceptRequest(r.getUrl()); }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u=r.getUrl(); String scheme=u.getScheme();
                if ("https".equals(scheme) || "http".equals(scheme)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW,u)); } catch(Exception e) { Toast.makeText(MainActivity.this,"No app found to open this link",Toast.LENGTH_SHORT).show(); }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if(fileCallback!=null) fileCallback.onReceiveValue(null); fileCallback=cb;
                Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*"); i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","text/html","text/plain"});
                try { startActivityForResult(i,PICK_FILE); } catch(Exception e) { fileCallback=null; cb.onReceiveValue(null); return false; }
                return true;
            }
        });
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_FILE && fileCallback!=null) {
            Uri[] result=(resultCode==RESULT_OK && data!=null && data.getData()!=null)?new Uri[]{data.getData()}:null;
            fileCallback.onReceiveValue(result); fileCallback=null;
        }
    }
    @Override public void onBackPressed() { if(web!=null && web.canGoBack()) web.goBack(); else super.onBackPressed(); }

    public class AndroidBridge {
        @JavascriptInterface public void saveBlob(String filename,String dataUrl) {
            try {
                int comma=dataUrl.indexOf(','); if(comma<0) throw new Exception("Invalid file data");
                byte[] bytes=Base64.decode(dataUrl.substring(comma+1),Base64.DEFAULT);
                String mime="application/octet-stream"; int semi=dataUrl.indexOf(';'); if(semi>5) mime=dataUrl.substring(5,semi);
                ContentValues values=new ContentValues(); values.put(MediaStore.Downloads.DISPLAY_NAME,filename);
                values.put(MediaStore.Downloads.MIME_TYPE,mime);
                values.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/Berry Prime Vissual");
                Uri uri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values);
                if(uri==null) throw new Exception("Could not create download");
                try(OutputStream out=getContentResolver().openOutputStream(uri)){out.write(bytes);}
                runOnUiThread(()->Toast.makeText(MainActivity.this,"Saved to Downloads/Berry Prime Vissual",Toast.LENGTH_LONG).show());
            } catch(Exception e) { runOnUiThread(()->Toast.makeText(MainActivity.this,"Save failed: "+e.getMessage(),Toast.LENGTH_LONG).show()); }
        }
    }
}
