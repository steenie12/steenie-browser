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
        TextView search=label("⌕",25,WHITE);bar.addView(search,new LinearLayout.LayoutParams(dp(30),dp(48)));
        address=new EditText(this);address.setSingleLine(true);address.setTextColor(WHITE);address.setHintTextColor(MUTED);address.setHint("Search or enter website");address.setTextSize(14);address.setBackgroundColor(Color.TRANSPARENT);address.setPadding(dp(5),0,dp(5),0);address.setSelectAllOnFocus(true);address.setImeOptions(EditorInfo.IME_ACTION_GO);bar.addView(address,new LinearLayout.LayoutParams(0,dp(48),1));
        address.setOnEditorActionListener((v,a,e)->{if(a==EditorInfo.IME_ACTION_GO||a==EditorInfo.IME_ACTION_DONE){navigate(address.getText().toString());hideKeyboard();return true;}return false;});
        TextView go=label("↗",21,WHITE);bar.addView(go,new LinearLayout.LayoutParams(dp(36),dp(48)));go.setOnClickListener(v->{navigate(address.getText().toString());hideKeyboard();});header.addView(bar,new LinearLayout.LayoutParams(-1,-2));
        pageFrame=new FrameLayout(this);root.addView(pageFrame,new LinearLayout.LayoutParams(-1,0,1));
        bottom=row();bottom.setPadding(dp(5),dp(4),dp(5),dp(5));bottom.setBackgroundColor(0xff10182a);root.addView(bottom,new LinearLayout.LayoutParams(-1,dp(62)));
        nav("←","Back",()->{if(web().canGoBack())web().goBack();});nav("→","Forward",()->{if(web().canGoForward())web().goForward();});nav("⌂","Home",this::goHome);nav("＋","New tab",()->addTab("",false));nav("☰","Menu",this::showMenu);
    }
    private void nav(String icon,String caption,Runnable action){LinearLayout item=column();item.setGravity(Gravity.CENTER);TextView i=label(icon,24,WHITE);item.addView(i);TextView c=label(caption,9,MUTED);item.addView(c);bottom.addView(item,new LinearLayout.LayoutParams(0,-1,1));item.setOnClickListener(v->action.run());}
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
    private String homeHtml(){
        String[] backs={
          "linear-gradient(180deg,rgba(5,8,18,.34),rgba(5,8,18,.94)),url('https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=1400&q=85')",
          "linear-gradient(180deg,rgba(4,12,23,.28),rgba(4,12,23,.94)),url('https://images.unsplash.com/photo-1470770841072-f978cf4d019e?auto=format&fit=crop&w=1400&q=85')",
          "linear-gradient(180deg,rgba(13,5,25,.32),rgba(8,5,19,.95)),url('https://images.unsplash.com/photo-1462331940025-496dfbfc7564?auto=format&fit=crop&w=1400&q=85')",
          "linear-gradient(180deg,rgba(7,10,24,.30),rgba(7,10,24,.95)),url('https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1400&q=85')"
        };
        String bg=backs[wallpaper%backs.length];
        String css="*{box-sizing:border-box}html{min-height:100%;background:#080b14}body{margin:0;min-height:100vh;color:#f7f8fc;font-family:Inter,Arial,sans-serif;background-image:"+bg+";background-position:center;background-size:cover;background-attachment:fixed;padding:24px 20px 34px}.wrap{max-width:620px;margin:0 auto}.top{display:flex;align-items:center;justify-content:space-between;color:#e9edf8;font-size:11px;letter-spacing:2.5px;font-weight:800}.live{display:flex;align-items:center;gap:7px;color:#c7d0e4;font-size:10px;letter-spacing:1.5px}.dot{width:7px;height:7px;border-radius:50%;background:#42e6a4;box-shadow:0 0 12px #42e6a4}.hero{text-align:center;padding:8vh 0 28px}.mark{width:76px;height:76px;margin:0 auto 18px;border-radius:26px;display:grid;place-items:center;background:linear-gradient(145deg,rgba(255,255,255,.24),rgba(255,255,255,.06));border:1px solid rgba(255,255,255,.38);box-shadow:0 18px 55px #0008,inset 0 1px 0 #ffffff55;backdrop-filter:blur(18px)}.mark svg{width:43px;height:43px;filter:drop-shadow(0 4px 12px #4c8cff99)}h1{font-size:32px;letter-spacing:5px;margin:0;font-weight:800;text-shadow:0 5px 24px #0009}.tag{font-size:12px;color:#c4ccdc;margin-top:10px;letter-spacing:.7px}.search{height:58px;max-width:560px;margin:30px auto 28px;display:flex;align-items:center;gap:13px;padding:0 18px;border:1px solid rgba(255,255,255,.24);border-radius:19px;background:rgba(14,20,34,.62);box-shadow:0 15px 45px #0005,inset 0 1px 0 #ffffff18;backdrop-filter:blur(22px)}.search svg{width:21px;height:21px;stroke:#e7edf9;fill:none;stroke-width:2}.search input{width:100%;border:0;outline:0;background:transparent;color:#fff;font-size:15px}.search input::placeholder{color:#b2bdcf}.search button{border:0;background:linear-gradient(135deg,#ff8a42,#f24f68);color:white;border-radius:12px;width:36px;height:36px;font-size:18px}.section{display:flex;align-items:center;justify-content:space-between;margin:0 2px 14px;color:#f1f4fa;font-size:12px;font-weight:700;letter-spacing:1.4px}.section span{font-size:10px;color:#b7c2d5;font-weight:500;letter-spacing:.5px}.grid{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}.tile{min-height:100px;text-decoration:none;color:#f5f7fb;text-align:center;padding:14px 4px 11px;border-radius:19px;border:1px solid rgba(255,255,255,.16);background:linear-gradient(145deg,rgba(255,255,255,.16),rgba(255,255,255,.055));box-shadow:0 12px 30px #0003,inset 0 1px 0 #ffffff20;backdrop-filter:blur(18px);transition:transform .16s,background .16s}.tile:active{transform:scale(.95);background:rgba(255,255,255,.24)}.ico{width:46px;height:46px;margin:0 auto 9px;display:grid;place-items:center;border-radius:15px;background:rgba(255,255,255,.95);box-shadow:0 6px 18px #0004}.ico svg{width:29px;height:29px}.tile b{display:block;font-size:11px;font-weight:600;letter-spacing:.1px}.lower{margin-top:27px;padding:16px 18px;border:1px solid #ffffff24;border-radius:18px;background:rgba(9,14,26,.48);backdrop-filter:blur(18px);display:flex;align-items:center;gap:13px}.lower svg{width:23px;height:23px;stroke:#aebbd1;fill:none;stroke-width:1.8;flex:none}.lower b{display:block;font-size:12px}.lower small{display:block;color:#aebbd1;font-size:10px;margin-top:4px}.foot{text-align:center;color:#aebbd1;font-size:10px;margin:24px 0 0;letter-spacing:.5px}@media(max-width:360px){body{padding:18px 13px}.grid{gap:8px}.tile{min-height:91px}.hero{padding-top:6vh}}";
        String html="<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1'><meta name='theme-color' content='#080b14'><link rel='preconnect' href='https://fonts.googleapis.com'><link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'><style>"+css+"</style></head><body><main class='wrap'><div class='top'><div>STEENIE <span style='color:#9db7ff'>BROWSER</span></div><div class='live'><i class='dot'></i> SHIELDS ACTIVE</div></div><section class='hero'><div class='mark'><svg viewBox='0 0 48 48' fill='none'><path d='M24 4 40 10v12c0 10-6.8 17.5-16 22C14.8 39.5 8 32 8 22V10L24 4Z' fill='url(#g)' stroke='white' stroke-opacity='.9' stroke-width='1.5'/><path d='m16 24 5 5 11-12' stroke='white' stroke-width='3.5' stroke-linecap='round' stroke-linejoin='round'/><defs><linearGradient id='g' x1='8' y1='4' x2='40' y2='44'><stop stop-color='#8bd4ff'/><stop offset='1' stop-color='#6252d9'/></linearGradient></defs></svg></div><h1>STEENIE</h1><div class='tag'>A calmer, faster place to explore the web.</div></section><form class='search' action='https://www.google.com/search'><svg viewBox='0 0 24 24'><circle cx='10.8' cy='10.8' r='6.8'/><path d='m16 16 4.3 4.3'/></svg><input name='q' placeholder='Search anything or enter a URL' autocomplete='off'><button aria-label='Search'>↗</button></form><div class='section'>QUICK ACCESS <span>YOUR FAVOURITES</span></div><div class='grid'>";
        html+="<a class='tile' href='https://www.google.com'><div class='ico'><svg viewBox='0 0 48 48'><path fill='#4285F4' d='M43.6 24.5c0-1.4-.1-2.8-.4-4.1H24v7.8h11a9.4 9.4 0 0 1-4.1 6.2v5.1h6.7c3.9-3.6 6-8.9 6-15Z'/><path fill='#34A853' d='M24 44c5.5 0 10.1-1.8 13.5-4.9l-6.7-5.1c-1.9 1.3-4.1 2-6.8 2-5.2 0-9.6-3.5-11.2-8.2H5.9v5.3A20 20 0 0 0 24 44Z'/><path fill='#FBBC05' d='M12.8 27.8a12 12 0 0 1 0-7.6v-5.3H5.9a20 20 0 0 0 0 18.2l6.9-5.3Z'/><path fill='#EA4335' d='M24 12.1c3 0 5.7 1 7.8 3.1l5.9-5.9A19.7 19.7 0 0 0 24 4 20 20 0 0 0 5.9 14.9l6.9 5.3C14.4 15.6 18.8 12.1 24 12.1Z'/></svg></div><b>Google</b></a>";
        html+="<a class='tile' href='https://mail.google.com'><div class='ico'><svg viewBox='0 0 48 48'><path fill='#4285F4' d='M7 13v24h7V20z'/><path fill='#34A853' d='M34 20v17h7V13z'/><path fill='#EA4335' d='M7 13l17 13 17-13v-2a4 4 0 0 0-6.4-3.2L24 17 13.4 7.8A4 4 0 0 0 7 11z'/><path fill='#C5221F' d='M7 13v-2a4 4 0 0 1 6.4-3.2L24 17v9z'/><path fill='#FBBC04' d='M41 13v-2a4 4 0 0 0-6.4-3.2L24 17v9z'/></svg></div><b>Gmail</b></a>";
        html+="<a class='tile' href='https://www.youtube.com'><div class='ico'><svg viewBox='0 0 48 48'><rect x='4' y='10' width='40' height='28' rx='9' fill='#FF0033'/><path d='m20 17 12 7-12 7V17Z' fill='white'/></svg></div><b>YouTube</b></a>";
        html+="<a class='tile' href='https://github.com'><div class='ico'><svg viewBox='0 0 48 48'><path fill='#171717' d='M24 4a20 20 0 0 0-6.3 39c1 .2 1.3-.4 1.3-.9v-3.4c-5.5 1.2-6.7-2.3-6.7-2.3-.9-2.3-2.2-2.9-2.2-2.9-1.8-1.2.1-1.2.1-1.2 2 .1 3 2 3 2 1.8 3 4.6 2.1 5.8 1.6.2-1.3.7-2.1 1.2-2.6-4.4-.5-9-2.2-9-9.8 0-2.2.8-4 2-5.4-.2-.5-.9-2.6.2-5.4 0 0 1.7-.5 5.5 2.1a19 19 0 0 1 10 0c3.8-2.6 5.5-2.1 5.5-2.1 1.1 2.8.4 4.9.2 5.4 1.3 1.4 2 3.2 2 5.4 0 7.6-4.6 9.3-9 9.8.7.6 1.3 1.8 1.3 3.6v5.3c0 .5.3 1.1 1.3.9A20 20 0 0 0 24 4Z'/></svg></div><b>GitHub</b></a>";
        html+="<a class='tile' href='https://www.wikipedia.org'><div class='ico'><svg viewBox='0 0 48 48'><circle cx='24' cy='24' r='20' fill='#20242b'/><text x='24' y='32' font-size='25' font-family='Georgia' text-anchor='middle' fill='white'>W</text></svg></div><b>Wikipedia</b></a>";
        html+="<a class='tile' href='https://www.facebook.com'><div class='ico'><svg viewBox='0 0 48 48'><circle cx='24' cy='24' r='22' fill='#0866FF'/><path fill='white' d='M27 42V26h5l.8-6H27v-3.8c0-1.8.6-3 3-3h3V8c-.5-.1-2.4-.2-4.5-.2-4.5 0-7.5 2.7-7.5 7.7V20h-5v6h5v16z'/></svg></div><b>Facebook</b></a>";
        html+="<a class='tile' href='https://www.instagram.com'><div class='ico'><svg viewBox='0 0 48 48'><defs><linearGradient id='ig' x1='5' y1='43' x2='43' y2='5'><stop stop-color='#FFB13D'/><stop offset='.48' stop-color='#E1306C'/><stop offset='1' stop-color='#833AB4'/></linearGradient></defs><rect x='5' y='5' width='38' height='38' rx='12' fill='url(#ig)'/><rect x='13' y='13' width='22' height='22' rx='7' fill='none' stroke='white' stroke-width='3'/><circle cx='24' cy='24' r='5' fill='none' stroke='white' stroke-width='3'/><circle cx='32' cy='16' r='2' fill='white'/></svg></div><b>Instagram</b></a>";
        html+="<a class='tile' href='https://www.reddit.com'><div class='ico'><svg viewBox='0 0 48 48'><circle cx='24' cy='24' r='21' fill='#FF4500'/><path d='M35 25c0-2-1.7-3.5-3.7-3.5-1 0-1.9.4-2.6 1.1a17 17 0 0 0-8.4 0l1.8-6.1 5 1.1a3 3 0 1 0 .5-2.4l-6.4-1.4-2.5 8.5c-.8-.5-1.7-.8-2.7-.8-2 0-3.7 1.5-3.7 3.5 0 1.3.7 2.4 1.8 3.1-.1.4-.2.8-.2 1.2 0 4.2 4.7 7.6 10.5 7.6s10.5-3.4 10.5-7.6c0-.4-.1-.8-.2-1.2 1.1-.7 1.8-1.8 1.8-3.1Z' fill='white'/><circle cx='19' cy='26' r='1.5' fill='#FF4500'/><circle cx='29' cy='26' r='1.5' fill='#FF4500'/><path d='M20 31c2.5 1.8 5.5 1.8 8 0' fill='none' stroke='#FF4500' stroke-width='1.5' stroke-linecap='round'/></svg></div><b>Reddit</b></a></div>";
        html+="<div class='lower'><svg viewBox='0 0 24 24'><path d='M12 3 20 6v6c0 5-3.4 8.8-8 11-4.6-2.2-8-6-8-11V6l8-3Z'/><path d='m8.5 12 2.3 2.3 4.8-5'/></svg><div><b>Your browsing, your rules</b><small>Tracker protection is enabled. Open the menu to switch wallpapers or start a private tab.</small></div></div><div class='foot'>STEENIE BROWSER · BUILT FOR YOUR EVERYDAY WEB</div></main></body></html>";
        return html;
    }
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
