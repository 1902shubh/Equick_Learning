package missy.tech.equicklearning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.webkit.WebSettings.LayoutAlgorithm.NARROW_COLUMNS;

public class MainActivity extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 45;
    private WebView web;
    String webUrl = "http://www.equicklearning.com/";
    RelativeLayout relativeLayout;
    Button NointernetBtn;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private InterstitialAd mInterstitialAd;
    private AppUpdateManager appUpdateManager;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForUpdates();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6662152927369982/8228548074");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLeftApplication() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        web = (WebView) findViewById(R.id.myweb);
        //internet connection check
        NointernetBtn = (Button) findViewById(R.id.btnRetry);
        relativeLayout = (RelativeLayout) findViewById(R.id.nointernet);


        web.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                internetcheck();
                super.onReceivedError(view, request, error);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String webUrl) {
                if (webUrl == null || webUrl.startsWith("http://") || webUrl.startsWith("https://"))
                    return false;
                else {
                    try {
                        String[] ur = webUrl.split("=");
                        String[] u = ur[1].split("&");
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+u[0]));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        String TAG = "Error";
                        Log.i(TAG, "shouldOverrideUrlLoading Exception:" + e);
                        return true;
                    }
                }
            }

         /*   public boolean shouldOverrideUrlLoading(WebView view, String webUrl){
                if(webUrl.startsWith("http:") || webUrl.startsWith("https:")){
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://equicklearning.com"));
                startActivity(intent);
                return true;
            }*/
        });

        WebSettings mywebsettings = web.getSettings();
        mywebsettings.setJavaScriptEnabled(true);

        web.loadUrl(webUrl);
        //improve webview performance
        web.getSettings().setLoadsImagesAutomatically(true);
        web.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        web.clearCache(true);
        web.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mywebsettings.setDomStorageEnabled(true);
        mywebsettings.setLayoutAlgorithm(NARROW_COLUMNS);
        mywebsettings.setUseWideViewPort(true);
        mywebsettings.setSavePassword(true);
        mywebsettings.setSaveFormData(true);
        mywebsettings.setEnableSmoothTransition(true);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        web.setWebChromeClient(new WebChromeClient() {

            // page loading progress, gone when fully loaded
            public void onProgressChanged(WebView view, int progress) {


                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }

                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                }
                progressBar.setProgress(progress);


            }
        });
        //pull to refresh

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        web.reload();
                    }
                }, 2000);
            }
        });

        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_dark),
                getResources().getColor(android.R.color.holo_orange_dark),
                getResources().getColor(android.R.color.holo_green_dark),
                getResources().getColor(android.R.color.holo_red_dark)

        );
        //internet connection check

        NointernetBtn = (Button) findViewById(R.id.btnRetry);
        relativeLayout = (RelativeLayout) findViewById(R.id.nointernet);

        internetcheck();

        NointernetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                internetcheck();
            }
        });

//External storage permission for saving file

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to WRITE_EXTERNAL_STORAGE - requesting it");
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 1);
            }


        }

        //handle downloading

        web.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file....");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_SHORT).show();


            }
        });

    }

    private void checkForUpdates() {
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(this);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    // Request the update.
                    showUpdateDialog(appUpdateInfo);
                }

            }
        });

    }

    private void showUpdateDialog(final AppUpdateInfo appUpdateInfo) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setMessage("New update Available")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateApp(appUpdateInfo);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog1 = dialog.create();
        dialog1.show();

    }

    private void updateApp(AppUpdateInfo appUpdateInfo) {
        try {

            appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.FLEXIBLE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    MY_REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            Toast.makeText(this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    public void onBackPressed() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    finish();

                }
            });
        }
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }

    }

    public void internetcheck() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobiledata = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        assert mobiledata != null;
        if (mobiledata.isConnected()) {
            web.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);
            web.reload();


        } else {
            assert wifi != null;
            if (wifi.isConnected()) {

                web.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                relativeLayout.setVisibility(View.GONE);
                web.reload();
            } else {

                web.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);

            }
        }
    }

}