package com.steenie.browser;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import java.io.ByteArrayInputStream;
import java.util.*;

public class MainActivity extends Activity {
    private static final int BG=0xff090d18, PANEL=0xff111a2c, PANEL2=0xff17233a, BLUE=0xff28aaff, PURPLE=0xff9875ff, WHITE=0xfff4f7ff, MUTED=0xff9ba9c2;
    private LinearLayout root, header, bottom;
    private EditText address;
    private FrameLayout pageFrame;
    private TextView tabCount, pageTitle;
    private final ArrayList<BrowserTab> tabs=new ArrayList<>();
    private int current=0, wallpaper=0;
    private boolean shields=true, desktop=false;
    private android.content.SharedPreferences prefs;

    private static class BrowserTab {
        WebView view;
        boolean incognito;
        String lastUrl="";
        BrowserTab(WebView v, boolean p){view=v;incognito=p;}
    }

    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(0xff070a12);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);prefs=getSharedPreferences("steenie_browser",0);wallpaper=prefs.getInt("wallpaper",0);buildUi();addTab("",false);}
    private GradientDrawable shape(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);t.setIncludeFontPadding(false);return t;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}

    private void buildUi(){
        root=column();root.setBackgroundColor(BG);setContentView(root);
        header=column();header.setPadding(dp(14),dp(8),dp(14),dp(10));header.setBackgroundColor(BG);root.addView(header,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout brand=row();
        TextView logo=label("S",22,WHITE);logo.setTypeface(Typeface.DEFAULT,Typeface.BOLD);logo.setBackground(shape(0xff182b4d,30,BLUE));brand.addView(logo,new LinearLayout.LayoutParams(dp(38),dp(38)));
        LinearLayout brandText=column();brandText.setPadding(dp(10),0,0,0);TextView name=label("STEENIE",17,WHITE);name.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);brandText.addView(name);TextView sub=label("PRIVATE • FAST • YOURS",9,MUTED);sub.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);brandText.addView(sub);brand.addView(brandText,new LinearLayout.LayoutParams(0,dp(42),1));
        TextView shield=label("◇",25,shields?BLUE:MUTED);brand.addView(shield,new LinearLayout.LayoutParams(dp(42),dp(42)));shield.setOnClickListener(v->{shields=!shields;shield.setTextColor(shields?BLUE:MUTED);toast(shields?"Shields enabled":"Shields paused");});
        tabCount=label("▢ 1",14,WHITE);brand.addView(tabCount,new LinearLayout.LayoutParams(dp(48),dp(42)));tabCount.setOnClickListener(v->showTabs());
        TextView menu=label("⋮",26,WHITE);brand.addView(menu,new LinearLayout.LayoutParams(dp(34),dp(42)));menu.setOnClickListener(v->showMenu());header.addView(brand);

        LinearLayout bar=row();bar.setPadding(dp(13),0,dp(8),0);bar.setBackground(shape(PANEL,28,0xff263858));
        TextView search=label("⌕",25,BLUE);bar.addView(search,new LinearLayout.LayoutParams(dp(30),dp(48)));
        address=new EditText(this);address.setSingleLine(true);address.setTextColor(WHITE);address.setHintTextColor(MUTED);address.setHint("Search or enter website");address.setTextSize(14);address.setBackgroundColor(Color.TRANSPARENT);address.setPadding(dp(5),0,dp(5),0);address.setSelectAllOnFocus(true);address.setImeOptions(EditorInfo.IME_ACTION_GO);bar.addView(address,new LinearLayout.LayoutParams(0,dp(48),1));
        address.setOnEditorActionListener((v,a,e)->{if(a==EditorInfo.IME_ACTION_GO||a==EditorInfo.IME_ACTION_DONE){navigate(address.getText().toString());hideKeyboard();return true;}return false;});
        TextView go=label("➜",21,BLUE);bar.addView(go,new LinearLayout.LayoutParams(dp(36),dp(48)));go.setOnClickListener(v->{navigate(address.getText().toString());hideKeyboard();});header.addView(bar,new LinearLayout.LayoutParams(-1,-2));
        pageFrame=new FrameLayout(this);root.addView(pageFrame,new LinearLayout.LayoutParams(-1,0,1));
        bottom=row();bottom.setPadding(dp(5),dp(4),dp(5),dp(5));bottom.setBackgroundColor(0xff10182a);root.addView(bottom,new LinearLayout.LayoutParams(-1,dp(62)));
        nav("‹","Back",()->{if(web().canGoBack())web().goBack();});nav("›","Forward",()->{if(web().canGoForward())web().goForward();});nav("⌂","Home",this::goHome);nav("＋","New tab",()->addTab("",false));nav("☰","Menu",this::showMenu);
    }
    private void nav(String icon,String caption,Runnable action){LinearLayout item=column();item.setGravity(Gravity.CENTER);TextView i=label(icon,24,BLUE);item.addView(i);TextView c=label(caption,9,MUTED);item.addView(c);bottom.addView(item,new LinearLayout.LayoutParams(0,-1,1));item.setOnClickListener(v->action.run());}
    private BrowserTab tab(){return tabs.get(current);} private WebView web(){return tab().view;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private void hideKeyboard(){((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);}

    private void addTab(String url,boolean incognito){
        WebView w=new WebView(this);w.setBackgroundColor(BG);WebSettings s=w.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setLoadsImagesAutomatically(true);s.setSupportZoom(true);s.setBuiltInZoomControls(true);s.setDisplayZoomControls(false);s.setJavaScriptCanOpenWindowsAutomatically(false);s.setAllowFileAccess(false);s.setCacheMode(incognito?WebSettings.LOAD_NO_CACHE:WebSettings.LOAD_DEFAULT);if(desktop)s.setUserAgentString(s.getUserAgentString().replace("Mobile",""));
        CookieManager.getInstance().setAcceptCookie(!incognito);
        if(incognito){w.clearCache(true);w.clearHistory();w.clearFormData();}
        w.setDownloadListener((u,ua,cd,mt,len)->startDownload(u,cd,mt));
        w.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView view,int progress){if(view==web()&&progress==100)syncAddress();}});
        w.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,String u){if(u.startsWith("http://")||u.startsWith("https://"))return false;try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception ignored){}return true;}
            @Override public void onPageStarted(WebView v,String u,android.graphics.Bitmap f){if(v==web())address.setText(u.startsWith("https://steenie.home/")?"":u);}
            @Override public void onPageFinished(WebView v,String u){BrowserTab bt=find(v);if(bt!=null){bt.lastUrl=u;if(!bt.incognito&&!u.startsWith("https://steenie.home/"))prefs.edit().putString("history",u+"\n"+prefs.getString("history","")).apply();}if(v==web())syncAddress();}
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest req){if(shields&&isTracker(req.getUrl().getHost()))return new WebResourceResponse("text/plain","utf-8",new ByteArrayInputStream(new byte[0]));return super.shouldInterceptRequest(view,req);}
        });
        tabs.add(new BrowserTab(w,incognito));current=tabs.size()-1;showCurrent();if(url==null||url.isEmpty())loadHome();else w.loadUrl(url);updateTabs();
    }
    private BrowserTab find(WebView v){for(BrowserTab t:tabs)if(t.view==v)return t;return null;}
    private boolean isTracker(String h){if(h==null)return false;String[] bad={"doubleclick.net","googlesyndication.com","google-analytics.com","googletagmanager.com","facebook.net","hotjar.com","scorecardresearch.com","adservice.google.com","adnxs.com","taboola.com"};for(String s:bad)if(h.equals(s)||h.endsWith("."+s))return true;return false;}
    private void syncAddress(){String u=web().getUrl();address.setText(u==null||u.startsWith("data:text/html")||u.startsWith("https://steenie.home/")?"":u);}
    private void showCurrent(){pageFrame.removeAllViews();if(tabs.size()>0){CookieManager.getInstance().setAcceptCookie(!tab().incognito);WebView w=web();if(w.getParent()!=null)((ViewGroup)w.getParent()).removeView(w);pageFrame.addView(w,new FrameLayout.LayoutParams(-1,-1));syncAddress();}}
    private void updateTabs(){if(tabCount!=null)tabCount.setText((tab().incognito?"◉ ":"▢ ")+tabs.size());}
    private void navigate(String input){if(input==null||input.trim().isEmpty())return;String s=input.trim();String u;if(s.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*"))u=s;else if(s.contains(".")&&!s.contains(" "))u="https://"+s;else u="https://www.google.com/search?q="+Uri.encode(s);web().loadUrl(u);}
    private void goHome(){loadHome();}
    private void loadHome(){address.setText("");web().loadDataWithBaseURL("https://steenie.home/",homeHtml(),"text/html","UTF-8",null);}
    private String homeHtml(){String[] backs={"linear-gradient(145deg,#080d19,#101a32 55%,#211746)","linear-gradient(145deg,#080d19,#102c48 55%,#211746)","linear-gradient(145deg,#080d19,#211746 55%,#12334a)","linear-gradient(145deg,#090d18,#25163e 55%,#12304a)"};String bg=backs[wallpaper%backs.length];return "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'><style>*{box-sizing:border-box}body{margin:0;color:#f4f7ff;font-family:Arial,sans-serif;background:"+bg+";min-height:100vh;padding:34px 22px}.brand{font-size:12px;letter-spacing:4px;color:#9ba9c2;text-align:center;margin-top:20px}.logo{margin:28px auto 8px;width:66px;height:66px;border-radius:22px;border:1px solid #28aaff;background:linear-gradient(145deg,#182b4d,#211746);display:flex;align-items:center;justify-content:center;font-size:38px;font-weight:bold;box-shadow:0 0 35px #247de755}.title{text-align:center;font-size:27px;font-weight:700;letter-spacing:1px}.tag{text-align:center;color:#9ba9c2;font-size:12px;margin:8px 0 30px}.search{display:flex;background:#111a2c;border:1px solid #334565;border-radius:28px;padding:15px 17px;margin:0 auto 32px;max-width:540px;color:#9ba9c2}.search input{border:0;outline:0;background:transparent;color:white;width:100%;font-size:15px}.grid{display:grid;grid-template-columns:repeat(4,1fr);gap:18px;max-width:500px;margin:auto}.tile{text-decoration:none;color:#e8edfa;text-align:center;font-size:11px}.ico{margin:0 auto 9px;width:52px;height:52px;border-radius:18px;background:#17233a;border:1px solid #304362;display:flex;align-items:center;justify-content:center;font-size:23px;color:#55baff}.note{text-align:center;color:#73829e;font-size:11px;margin-top:45px}</style></head><body><div class='brand'>YOUR WEB. YOUR RULES.</div><div class='logo'>S</div><div class='title'>STEENIE</div><div class='tag'>A brighter way to browse</div><form class='search' action='https://www.google.com/search'><span style='font-size:20px;margin-right:12px'>⌕</span><input name='q' placeholder='Search the web or enter a website'></form><div class='grid'><a class='tile' href='https://www.google.com'><div class='ico'>G</div>Google</a><a class='tile' href='https://mail.google.com'><div class='ico'>✉</div>Gmail</a><a class='tile' href='https://www.youtube.com'><div class='ico'>▶</div>YouTube</a><a class='tile' href='https://github.com'><div class='ico'>⌘</div>GitHub</a><a class='tile' href='https://www.wikipedia.org'><div class='ico'>W</div>Wikipedia</a><a class='tile' href='https://www.facebook.com'><div class='ico'>f</div>Facebook</a><a class='tile' href='https://www.instagram.com'><div class='ico'>◎</div>Instagram</a><a class='tile' href='https://www.reddit.com'><div class='ico'>◉</div>Reddit</a></div><div class='note'>Shields are on • Tap ⋮ to customize your browser</div></body></html>";}
    private void startDownload(String u,String cd,String mt){try{DownloadManager.Request r=new DownloadManager.Request(Uri.parse(u));r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);r.setTitle(URLUtil.guessFileName(u,cd,mt));r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,URLUtil.guessFileName(u,cd,mt));((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(r);toast("Download started");}catch(Exception e){toast("Download unavailable: "+e.getMessage());}}

    private void showTabs(){if(tabs.isEmpty())return;String[] names=new String[tabs.size()];for(int i=0;i<tabs.size();i++){String title=tabs.get(i).view.getTitle();names[i]=(i==current?"✓ ":"")+(tabs.get(i).incognito?"◉ Private · ":"")+(title==null||title.isEmpty()?"New tab":title);}new AlertDialog.Builder(this).setTitle("Tabs · "+tabs.size()).setItems(names,(d,n)->{current=n;showCurrent();updateTabs();}).setPositiveButton("＋ New tab",(d,n)->addTab("",false)).setNeutralButton("Close current",(d,n)->closeTab()).setNegativeButton("Done",null).show();}
    private void closeTab(){if(tabs.size()<=1){web().loadUrl("about:blank");loadHome();return;}BrowserTab old=tab();tabs.remove(current);old.view.stopLoading();old.view.loadUrl("about:blank");old.view.destroy();current=Math.max(0,current-1);showCurrent();updateTabs();}
    private void showMenu(){String[] options={"＋  New tab","◉  New private tab","✉  Open Gmail","☆  Add bookmark","▣  Bookmarks","◷  History","⇩  Downloads","▧  Change background","▤  Desktop site: "+(desktop?"ON":"OFF"),"◇  Shields: "+(shields?"ON":"OFF"),"⚙  Settings & privacy"};new AlertDialog.Builder(this).setTitle("STEENIE BROWSER").setItems(options,(d,n)->{switch(n){case 0:addTab("",false);break;case 1:addTab("",true);toast("Private tab opened. Local history is not saved.");break;case 2:if(tab().incognito)web().loadUrl("https://mail.google.com");else addTab("https://mail.google.com",false);break;case 3:saveBookmark();break;case 4:showSaved("Bookmarks","bookmarks");break;case 5:showSaved("History","history");break;case 6:try{startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));}catch(Exception e){toast("Downloads unavailable");}break;case 7:chooseWallpaper();break;case 8:desktop=!desktop;toast("Desktop mode "+(desktop?"enabled":"disabled")+" for new tabs");break;case 9:shields=!shields;toast("Shields "+(shields?"enabled":"paused"));break;case 10:showSettings();break;}}).show();}
    private void saveBookmark(){String u=web().getUrl();if(u==null||u.startsWith("data:")){toast("Open a website first");return;}prefs.edit().putString("bookmarks",u+"\n"+prefs.getString("bookmarks","")).apply();toast("Bookmark saved");}
    private void showSaved(String title,String key){String raw=prefs.getString(key,"");ArrayList<String> items=new ArrayList<>();for(String s:raw.split("\n"))if(s.startsWith("http")&&!items.contains(s))items.add(s);if(items.isEmpty()){toast("Nothing here yet");return;}new AlertDialog.Builder(this).setTitle(title).setItems(items.toArray(new String[0]),(d,n)->navigate(items.get(n))).setPositiveButton("Close",null).show();}
    private void chooseWallpaper(){String[] choices={"Midnight blue & violet","Deep ocean blue","Violet galaxy","Electric dusk"};new AlertDialog.Builder(this).setTitle("Choose your background").setSingleChoiceItems(choices,wallpaper,(d,n)->{wallpaper=n;prefs.edit().putInt("wallpaper",wallpaper).apply();d.dismiss();loadHome();toast("Background updated");}).setNegativeButton("Cancel",null).show();}
    private void showSettings(){new AlertDialog.Builder(this).setTitle("Settings & privacy").setMessage("STEENIE BROWSER 2.0\n\nGmail: Opens Gmail in a browser tab. Sign in on Google's website; STEENIE does not store your Google password.\n\nPrivate tabs: Do not save local browsing history and use no-cache mode with cookies disabled while active. This is not full anonymity.\n\nShields: Blocks a small built-in list of known tracker domains. It is not a complete ad blocker.\n\nEngine: Android System WebView (Chromium-based), updated by your device provider.\n\nBackgrounds: Choose a built-in start-page theme from the menu.").setPositiveButton("OK",null).show();}
    @Override public void onBackPressed(){if(!tabs.isEmpty()&&web().canGoBack())web().goBack();else goHome();}
    @Override protected void onDestroy(){for(BrowserTab t:tabs){try{t.view.stopLoading();t.view.destroy();}catch(Exception ignored){}}tabs.clear();super.onDestroy();}
}
