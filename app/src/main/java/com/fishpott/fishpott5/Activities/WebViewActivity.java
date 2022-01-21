package com.fishpott.fishpott5.Activities;

import android.graphics.Bitmap;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.R;

public class WebViewActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mUrlTextView;
    private ImageView mHttpsLockImageView, mBackImageView;
    private WebView mWebView;
    private ProgressBar mPageLoadingProgressBar;
    private String websiteUrl = "", domainName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        // BINDING VIEWS
        mUrlTextView = findViewById(R.id.activity_webview_constraint2_title_textview);
        mHttpsLockImageView = findViewById(R.id.activity_webview_padlock_imageView);
        mWebView = findViewById(R.id.activity_webview_webview);
        mBackImageView = findViewById(R.id.activity_webview_back_imageview);
        mPageLoadingProgressBar = findViewById(R.id.activity_webview_loader);

        if(getIntent().getExtras() !=null) {
            websiteUrl =(String) getIntent().getExtras().get(Config.WEBVIEW_KEY_URL);
            domainName = Config.getUrlComponent(websiteUrl, 1);
            domainName = Config.removeWwwAndHttpFromUrl(domainName);
            mPageLoadingProgressBar.setVisibility(View.INVISIBLE);
        } else {
            finish();
        }

        if(domainName.trim().equalsIgnoreCase("")){
            mUrlTextView.setText("External Link");
        } else {
            mUrlTextView.setText(domainName);
        }

        if(Config.getUrlComponent(websiteUrl, 2).trim().equalsIgnoreCase("https")){
            mHttpsLockImageView.setImageResource(R.drawable.webview_activity_lock_image);
            mHttpsLockImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.color_webview_activity_secure_padlock), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            mHttpsLockImageView.setImageResource(R.drawable.webview_activity_open_lock);
            mHttpsLockImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.color_webview_activity_insecure_padlock), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new MyBrowser());
        mWebView.loadUrl(websiteUrl);
        mWebView.setWebChromeClient(new WebChromeClient());

        mBackImageView.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.activity_webview_back_imageview){
            onBackPressed();
        }
    }

    private class MyBrowser extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mPageLoadingProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mPageLoadingProgressBar.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()){
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED  WEBVIEW-ACTIVITY");
        mUrlTextView = findViewById(R.id.activity_webview_constraint2_title_textview);
        mHttpsLockImageView = findViewById(R.id.activity_webview_padlock_imageView);
        mWebView = findViewById(R.id.activity_webview_webview);
        mBackImageView = findViewById(R.id.activity_webview_back_imageview);
        mPageLoadingProgressBar = findViewById(R.id.activity_webview_loader);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED  WEBVIEW-ACTIVITY");
        mUrlTextView = null;
        mHttpsLockImageView = null;
        mBackImageView = null;
        mWebView = null;
        mPageLoadingProgressBar = null;
        websiteUrl = null;
        domainName = null;
        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        Log.e("memoryManage", "finish STARTED  WEBVIEW-ACTIVITY");
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        Config.unbindDrawables(findViewById(R.id.root_webview_activity));
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED  WEBVIEW-ACTIVITY");
        Config.unbindDrawables(findViewById(R.id.root_webview_activity));
        Config.freeMemory();
    }
}
