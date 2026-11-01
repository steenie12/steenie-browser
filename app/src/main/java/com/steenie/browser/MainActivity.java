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
import android.webkit.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private final int BG=Color.rgb(8,11,20), PANEL=Color.rgb(17,24,42), BLUE=Color.rgb(34,167,255), PURPLE=Color.rgb(139,92,246), WHITE=Color.WHITE, MUTED=Color.rgb(153,166,190);
    private LinearLayout root, top, bottom; private EditText address; private FrameLayout pageFrame; private TextView tabsLabel, status;
    private final ArrayList<WebView> tabs=new ArrayList<>(); private int current=0; private boolean privateMode=false, desktop=false, shield=true;
    private android.content.SharedPreferences prefs;
    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);prefs=getSharedPreferences("steenie",0);build(); addTab("https://www.google.com");}
    private GradientDrawable shape(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);if(stroke!=0)d.setStroke(1,stroke);return d;}
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(17);return t;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private void build(){
        root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(BG);setContentView(root);
        top=new LinearLayout(this);top.setPadding(12,10,12,8);top.setOrientation(1);top.setBackgroundColor(BG);root.addView(top,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout brand=row(); TextView logo=text("S",22,WHITE);logo.setTypeface(null,Typeface.BOLD);logo.setBackground(shape(0xff17264a,40,BLUE));brand.addView(logo,new LinearLayout.LayoutParams(42,42));
        TextView title=text("  STEENIE",18,WHITE);title.setGravity(Gravity.CENTER_VERTICAL);title.setTypeface(null,Typeface.BOLD);brand.addView(title,new LinearLayout.LayoutParams(0,44,1));
        TextView shieldBtn=text("◈",24,shield?BLUE:MUTED);brand.addView(shieldBtn,new LinearLayout.LayoutParams(42,42));shieldBtn.setOnClickListener(v->{shield=!shield;shieldBtn.setTextColor(shield?BLUE:MUTED);Toast.makeText(this,shield?"Protection enabled":"Protection paused",0).show();});
        tabsLabel=text("▢ 1",15,WHITE);TextView tabBtn=tabsLabel;brand.addView(tabBtn,new LinearLayout.LayoutParams(54,42));tabBtn.setOnClickListener(v->showTabs());top.addView(brand);
        LinearLayout bar=row();bar.setPadding(12,0,8,0);bar.setBackground(shape(PANEL,28,0xff263653));
        TextView searchIcon=text("⌕",24,BLUE);bar.addView(searchIcon,new LinearLayout.LayoutParams(30,46));
        address=new EditText(this);address.setSingleLine(true);address.setTextColor(WHITE);address.setHintTextColor(MUTED);address.setHint("Search or enter address");address.setTextSize(14);address.setBackgroundColor(Color.TRANSPARENT);address.setPadding(4,0,4,0);address.setImeOptions(EditorInfo.IME_ACTION_GO);bar.addView(address,new LinearLayout.LayoutParams(0,46,1));
        address.setOnEditorActionListener((v,a,e)->{if(a==EditorInfo.IME_ACTION_GO||a==EditorInfo.IME_ACTION_DONE){navigate(address.getText().toString());((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);return true;}return false;});
        TextView go=text("➜",19,BLUE);bar.addView(go,new LinearLayout.LayoutParams(34,46));go.setOnClickListener(v->navigate(address.getText().toString()));top.addView(bar,new LinearLayout.LayoutParams(-1,-2));
        pageFrame=new FrameLayout(this);root.addView(pageFrame,new LinearLayout.LayoutParams(-1,0,1));
        bottom=row();bottom.setPadding(8,5,8,8);bottom.setBackgroundColor(PANEL);root.addView(bottom,new LinearLayout.LayoutParams(-1,56));
        addNav("‹","Back",()->{WebView w=web();if(w.canGoBack())w.goBack();});addNav("›","Forward",()->{WebView w=web();if(w.canGoForward())w.goForward();});addNav("⌂","Home",()->navigate("https://www.google.com"));addNav("＋","New tab",()->addTab("https://www.google.com"));addNav("⋮","Menu",this::showMenu);
    }
    private void addNav(String icon,String label,Runnable action){LinearLayout item=new LinearLayout(this);item.setOrientation(1);item.setGravity(17);TextView i=text(icon,23,BLUE);TextView l=text(label,9,MUTED);item.addView(i);item.addView(l);bottom.addView(item,new LinearLayout.LayoutParams(0,-1,1));item.setOnClickListener(v->action.run());}
    private WebView web(){return tabs.get(current);}
    private void addTab(String url){WebView w=new WebView(this);w.setBackgroundColor(BG);w.getSettings().setJavaScriptEnabled(true);w.getSettings().setDomStorageEnabled(true);w.getSettings().setLoadsImagesAutomatically(true);w.getSettings().setSupportZoom(true);w.getSettings().setBuiltInZoomControls(true);w.getSettings().setDisplayZoomControls(false);w.getSettings().setUserAgentString(w.getSettings().getUserAgentString()+(desktop?" Desktop":""));CookieManager.getInstance().setAcceptCookie(!privateMode);w.setDownloadListener((u,ua,cd,mt,len)->{try{DownloadManager.Request r=new DownloadManager.Request(Uri.parse(u));r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);r.setTitle(URLUtil.guessFileName(u,cd,mt));r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,URLUtil.guessFileName(u,cd,mt));((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(r);Toast.makeText(this,"Download started",0).show();}catch(Exception ex){Toast.makeText(this,"Download unavailable: "+ex.getMessage(),1).show();}});
        w.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,String u){if(u.startsWith("http://")||u.startsWith("https://"))return false;try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception ignored){}return true;}
        @Override public void onPageStarted(WebView v,String u,android.graphics.Bitmap f){if(v==web())address.setText(u);}
        @Override public void onPageFinished(WebView v,String u){if(v==web()){address.setText(u);prefs.edit().putString("last",u).apply();}if(!privateMode)prefs.edit().putString("history",u+"\n"+prefs.getString("history","")).apply();}
        @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest req){if(shield&&isTracker(req.getUrl().getHost()))return new WebResourceResponse("text/plain","utf-8",new java.io.ByteArrayInputStream(new byte[0]));return super.shouldInterceptRequest(view,req);}});
        tabs.add(w);current=tabs.size()-1;showCurrent();w.loadUrl(url);updateTabs();}
    private boolean isTracker(String h){if(h==null)return false;String[] bad={"doubleclick.net","googlesyndication.com","google-analytics.com","googletagmanager.com","facebook.net","hotjar.com","scorecardresearch.com","adservice.google.com"};for(String s:bad)if(h.equals(s)||h.endsWith("."+s))return true;return false;}
    private void showCurrent(){pageFrame.removeAllViews();if(!tabs.isEmpty()){if(web().getParent()!=null)((android.view.ViewGroup)web().getParent()).removeView(web());pageFrame.addView(web(),new FrameLayout.LayoutParams(-1,-1));address.setText(web().getUrl()==null?"":web().getUrl());}}
    private void updateTabs(){if(tabsLabel!=null)tabsLabel.setText("▢ "+tabs.size());}
    private void navigate(String s){if(s==null||s.trim().isEmpty())return;s=s.trim();String u;if(s.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*"))u=s;else if(s.contains(".")&&!s.contains(" "))u="https://"+s;else u="https://www.google.com/search?q="+Uri.encode(s);web().loadUrl(u);}
    private void showTabs(){String[] names=new String[tabs.size()];for(int i=0;i<tabs.size();i++)names[i]=(i==current?"✓ ":"")+(tabs.get(i).getTitle()==null?"New tab":tabs.get(i).getTitle());new AlertDialog.Builder(this).setTitle("Your tabs").setItems(names,(d,n)->{current=n;showCurrent();}).setPositiveButton("New tab",(d,n)->addTab("https://www.google.com")).setNegativeButton("Close current",(d,n)->closeTab()).show();}
    private void closeTab(){if(tabs.size()<=1){web().loadUrl("https://www.google.com");return;}WebView old=web();tabs.remove(current);old.destroy();current=Math.max(0,current-1);showCurrent();updateTabs();}
    private void showMenu(){String[] opts={"New tab","New private tab","Add bookmark","Bookmarks","History","Downloads","Desktop site: "+(desktop?"ON":"OFF"),"Privacy protection: "+(shield?"ON":"OFF"),"Settings / About"};new AlertDialog.Builder(this).setTitle("STEENIE BROWSER").setItems(opts,(d,n)->{switch(n){case 0:addTab("https://www.google.com");break;case 1:privateMode=true;addTab("https://www.google.com");Toast.makeText(this,"Private tab (history not saved)",0).show();break;case 2:prefs.edit().putString("bookmarks",web().getUrl()+"\n"+prefs.getString("bookmarks","")).apply();Toast.makeText(this,"Bookmark saved",0).show();break;case 3:showSaved("Bookmarks","bookmarks");break;case 4:showSaved("History","history");break;case 5:startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));break;case 6:desktop=!desktop;Toast.makeText(this,"Open a new tab to apply desktop mode",0).show();break;case 7:shield=!shield;Toast.makeText(this,"Protection "+(shield?"enabled":"paused"),0).show();break;case 8:about();break;}}).show();}
    private void showSaved(String title,String key){String raw=prefs.getString(key,"");String[] a=raw.split("\n");ArrayList<String> clean=new ArrayList<>();for(String s:a)if(s.startsWith("http"))clean.add(s);if(clean.isEmpty()){Toast.makeText(this,"Nothing saved yet",0).show();return;}new AlertDialog.Builder(this).setTitle(title).setItems(clean.toArray(new String[0]),(d,n)->navigate(clean.get(n))).show();}
    private void about(){new AlertDialog.Builder(this).setTitle("STEENIE BROWSER 1.0").setMessage("A midnight-themed browser built with Android System WebView (Chromium-based). Includes tabs, bookmarks, history, downloads, private-tab mode and a basic tracker-domain blocklist.\n\nThis is not the full standalone Chromium engine. Private mode is basic and does not hide activity from websites, networks or your internet provider.").setPositiveButton("OK",null).show();}
    @Override public void onBackPressed(){if(!tabs.isEmpty()&&web().canGoBack())web().goBack();else super.onBackPressed();}
}
