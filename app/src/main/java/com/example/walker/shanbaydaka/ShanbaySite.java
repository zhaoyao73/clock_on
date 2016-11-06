package com.example.walker.shanbaydaka;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* either HttpURLConnection or apache's httpclient */
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/*
import org.apache.http.Header;
import org.apache.http.cookie.*;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
*/

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.HashMap;

/**
 * Created by yzhao on 17/09/16.
 */
public class ShanbaySite {
    private final String mShanbayHost = "www.shanbay.com";
    private final String mShanbayUrlBase = "https://" + mShanbayHost + "/";
    private final String mShanbayLogin = mShanbayUrlBase + "accounts/login" + "/";
    private String mShanbayMemberPage = mShanbayUrlBase + "team/members/#p1";
    private int mDebug = 4;
    /*
    private HttpClientBuilder mHttpBuilder;
    private HttpClient   mHttpClient;
    private CookieStore  mCookieStore;
    */
    private HttpsURLConnection mHttpClient;
    URL mUrl;
    private CookieManager mCookieManager;

    private final String mShanbayUserAgent = "Mozilla/5.0 (X11; Linux x86_64)";//AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36";
    private final String mShanbayAccept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    private final String mShanbayAcceptLanguage = "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4";
    private String mShanbayCookies;
    private List<Pair<String,String>> mPostParams;

    private String mPage, mMemberPage;
    private String mUsername, mPassword;
    private HashMap<String, ArrayList<String>> mMemberHash = null;
    private int mMemberPageCnt = -1;

    private String mWechatMessage = "It is time to punch the time card!";
    private String mMemberNoneCheckin = null;

    private void ShanbayDebug(int level, String output) {
    	if (level <= mDebug) {
    		System.out.println(output);
    	}
    }
    public ShanbaySite(String username, String password) {
        mCookieManager = new CookieManager();
        CookieHandler.setDefault(mCookieManager);
        /*
        mCookieStore = new BasicCookieStore();
        mHttpBuilder = HttpClientBuilder.create();
        mHttpBuilder.setDefaultCookieStore(mCookieStore);
        mHttpClient = mHttpBuilder.build();
        */
        mUsername = username;
        mPassword = password;
    }
    private void sendPost(String url, List<Pair<String,String>> postParams) throws Exception {
//
//        HttpPost post = new HttpPost(url);
//
//        // add header
//        post.setHeader("Host", mShanbayHost);
//        post.setHeader("User-Agent", mShanbayUserAgent);
//        post.setHeader("Accept", mShanbayAccept);
//        post.setHeader("Accept-Language", mShanbayAcceptLanguage);
//        post.setHeader("Connection", "keep-alive");
//
//        post.setHeader("Cookie", mShanbayCookies);
//        post.setHeader("Referer", mShanbayLogin);
//        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
//
//        post.setEntity(new UrlEncodedFormEntity(postParams));
//
//        HttpResponse response = mHttpClient.execute(post);
//
//        int responseCode = response.getStatusLine().getStatusCode();
//
//        ShanbayDebug(5, "\nSending 'POST' request to URL : " + url);
//        ShanbayDebug(5, "Post parameters : " + postParams);
//        ShanbayDebug(5, "Response Code : " + responseCode);
//
//        BufferedReader rd = new BufferedReader(
//                new InputStreamReader(response.getEntity().getContent()));
//
//        StringBuffer result = new StringBuffer();
//        String line = "";
//        while ((line = rd.readLine()) != null) {
//            result.append(line);
//        }
//
//        ShanbayDebug(6, result.toString());
//
//        /* https://www.shanbay.com/team/team/#p1 will get the team id
//         * <div class="my-team">
//         *           <a href="/team/detail/18031/">
//         * https://www.shanbay.com/team/members/#p1
//         *  will get all members for page1, need to parse html to get nickname...
//         *
//         *  https://www.shanbay.com/team/members/?page=25#p1
//         *              <li><a class="endless_page_link" href="/team/members/?page=25" rel="page">25</a></li>
//         *   <li><a class="endless_page_link" href="/team/members/?page=2" rel="page">&gt;&gt;</a></li>
//         * */
//        /* if we don't release it will stuck after twice,
//         * not exactly sure what this releaseConnection means,
//         * it seems it didn't shutdown the tcp connection
//         */
//        post.releaseConnection();

        URL ourl = new URL(url);
        mHttpClient = (HttpsURLConnection)ourl.openConnection();
        mHttpClient.setRequestMethod("POST");
        mHttpClient.setRequestProperty("Host", mShanbayHost);
        mHttpClient.setRequestProperty("User-Agent", mShanbayUserAgent);
        mHttpClient.setRequestProperty("Accept", mShanbayAccept);
        mHttpClient.setRequestProperty("Accept-Language", mShanbayAcceptLanguage);
        mHttpClient.setRequestProperty("Connection", "keep-alive");

        if (mCookieManager != null) {
            List<HttpCookie> ck = mCookieManager.getCookieStore().getCookies();

            if (ck != null && ck.size() > 0) {
                for(HttpCookie cookie : ck) {
                    mHttpClient.setRequestProperty("Cookie", cookie.getName() + "=" + cookie.getValue());
                }
            }

        }
        mHttpClient.setRequestProperty("Referer", mShanbayLogin);
        mHttpClient.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String PS = null;
        for(Pair<String, String> p : postParams) {
            if (PS == null) {
                PS = p.first + "=" + p.second;
            } else {
                PS = PS + "&" + p.first + "=" + p.second;
            }
        }
        String urlParameters = PS;//URLEncoder.encode(PS, "ISO-8859-1");
        mHttpClient.setDoInput(true);
        DataOutputStream wr = new DataOutputStream(mHttpClient.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        try {
            int responseCode = mHttpClient.getResponseCode();

            ShanbayDebug(5, "\nSending 'POST' request to URL : " + url);
            ShanbayDebug(5, "Post parameters : " + postParams);
            ShanbayDebug(5, "Response Code : " + responseCode);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(mHttpClient.getInputStream()));

            StringBuffer response = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();

            ShanbayDebug(6, response.toString());
        } finally {
            mHttpClient.disconnect();
        }
    }

    private String sendGet(String url) throws Exception {
//
//        HttpGet request = new HttpGet(url);
//
//        request.setHeader("Host", mShanbayHost);
//        request.setHeader("User-Agent", mShanbayUserAgent);
//        request.setHeader("Accept", mShanbayAccept);
//        request.setHeader("Accept-Language", mShanbayAcceptLanguage);
//        request.setHeader("Connection", "keep-alive");
//        if (mShanbayCookies != null && !mShanbayCookies.isEmpty()) {
//        	request.setHeader("Cookie", mShanbayCookies);
//        }
//
//        if (mDebug > 0) {
//            ShanbayDebug(5, request.toString());
//
//            Header ha[] = request.getAllHeaders();
//        	for (Header h : ha) {
//        		ShanbayDebug(5, h.getName() + ": " + h.getValue());
//        	}
//        }
//        HttpResponse response = mHttpClient.execute(request);
//        int responseCode = response.getStatusLine().getStatusCode();
//
//        ShanbayDebug(5, "\nSending 'GET' request to URL : " + url);
//        ShanbayDebug(5, "Response Code : " + responseCode);
//
//        BufferedReader rd = new BufferedReader(
//                new InputStreamReader(response.getEntity().getContent()));
//
//        StringBuffer result = new StringBuffer();
//        String line = "";
//        while ((line = rd.readLine()) != null) {
//            result.append(line);
//        }
//
//        // set cookies
//        if (true) {
//        	line = mCookieStore.getCookies().toString();
//	        System.out.println("Set-Cookie List:" + line);
//	        line = "";
//        	for(int i = 0; i < mCookieStore.getCookies().size(); i ++ ) {
//        		line = line + mCookieStore.getCookies().get(i).getName() + "=" +
//        				mCookieStore.getCookies().get(i).getValue() + "; ";
//        	}
//	        /* csrftoken=QI5bBhgAiH2x03xEe5D3t8XjoD5Z2VUj; SERVER_ID=e022a677-96be8784; userid=40800913; */
//        	ShanbayDebug(5, "result of Cookie:" + line);
//	        setCookies(line);
//        }
//
//        request.releaseConnection();
//        return result.toString();

        URL ourl = new URL(url);
        mHttpClient = (HttpsURLConnection)ourl.openConnection();

        try {
            mHttpClient.setRequestMethod("GET");
            mHttpClient.setRequestProperty("Host", mShanbayHost);
            mHttpClient.setRequestProperty("User-Agent", mShanbayUserAgent);
            mHttpClient.setRequestProperty("Accept", mShanbayAccept);
            mHttpClient.setRequestProperty("Accept-Language", mShanbayAcceptLanguage);
            mHttpClient.setRequestProperty("Connection", "keep-alive");
            if (mShanbayCookies != null && !mShanbayCookies.isEmpty()) {
                mHttpClient.setRequestProperty("Cookie", mShanbayCookies);
            }

            //mHttpClient.setDoOutput(true);
            int responseCode = mHttpClient.getResponseCode();

            ShanbayDebug(5, "\nSending 'GET' request to URL : " + url);
            ShanbayDebug(5, "Response Code : " + responseCode);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(mHttpClient.getInputStream()));

            StringBuffer response = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();

            ShanbayDebug(6, response.toString());

            Map<String, List<String>> hF = mHttpClient.getHeaderFields();
            List<String> cookiesHeader = hF.get("Set-Cookie");
            if (cookiesHeader != null) {
                for(String ckH : cookiesHeader) {
                    List<HttpCookie> cookies;
                    try {
                        cookies = HttpCookie.parse(ckH);
                    } catch (NullPointerException e) {
                        continue;
                    }
                    if (cookies != null) {
                        if (cookies.size() > 0) {
                            mCookieManager.getCookieStore().add(null, HttpCookie.parse(ckH).get(0));
                        }
                    }
                }
            }
            return response.toString();
        } finally {
            mHttpClient.disconnect();
        }

    }

    public List<Pair<String,String>> getFormParams(
            String html, String username, String password)
            throws UnsupportedEncodingException {

        ShanbayDebug(6, "Extracting form's data...");

        Document doc = Jsoup.parse(html);

        // Google form id
        Element script = doc.getElementById("login-form-tmpl");
       //Elements inputElements = doc.getElementsContainingText("<form action");
        Elements script_form = script.getElementsByTag("form");

        List<Pair<String, String>> paramList = new ArrayList<>();
	
        for (Element inputElement : script_form) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            if (key.equals("username"))
                value = username;
            else if (key.equals("passwd"))
                value = password;

            paramList.add(new Pair<>(key, value));
        }

        /* Jsoup may not support parse script */
        /*<script type="text/x-jquery-tmpl" id="login-form-tmpl">        <form action="${href}" method="post" class="account-form" id="loginform"> <input type='hidden' name='csrfmiddlewaretoken' value='gv8BvvrqgL5xfCPSS7eOskWXezKKqHa2' />    <div id="" class="">        <p><label for="id_username">用户名或邮箱或手机号：</label> <input class="text-input" id="id_username" name="username" type="text" /></p><p><label for="id_password">密码：</label> <input class="text-input" id="id_password" name="password" type="password" /><input id="id_token" name="token" placeholder="2-step token" type="hidden" /></p>        <p class="token hide">          <label for="id_token">两步验证密码</label>          <input class="text-input" id="id_token" type="text" name="token" value="" />          <span class="add-on-icon visible-desktop"><i class="icon-key"></i></span>        </p>    </div>    <div class="clear"></div>    <div class="login-buttons">        <button type="submit"  class="btn btn-success">登录</button>    </div></form>    </script>*/
        /* <script type="text/x-jquery-tmpl" id="login-form-tmpl">         <form action="${href}" method="post" class="account-form" id="loginform">
         *          <input type='hidden' name='csrfmiddlewaretoken' value='ZofKZpf9HQhzxhMPih81T0h12aL1fuy2' />
         *              <div id="" class="">        <p><label for="id_username">用户名或邮箱或手机号：</label> <input class="text-input" id="id_username" name="username" type="text" /></p>
         *              <p><label for="id_password">密码：</label> <input class="text-input" id="id_password" name="password" type="password" />
         *              <input id="id_token" name="token" placeholder="2-step token" type="hidden" /></p>
         *                      <p class="token hide">          <label for="id_token">两步验证密码</label>
         *                                <input class="text-input" id="id_token" type="text" name="token" value="" />
         *                                          <span class="add-on-icon visible-desktop"><i class="icon-key"></i></span>
         *                                                  </p>    </div>
         *                                                      <div class="clear"></div>    <div class="login-buttons">        <button type="submit"  class="btn btn-success">登录</button>    </div>
         *                                                      </form>
         *                                                          </script>
         *
         */

        if (paramList.isEmpty()) {
        	Pattern csrToken = Pattern.compile("input type=.+ name=\'(.+)\'.+value=\'(.+)\'.+input class=\".+\" id=\"id_username\" name=\"(.+)\" type=\"text\".+input class=\".+\" id=\"id_password\" name=\"(.+)\" type=\"password\"");
        	Matcher regMatch = csrToken.matcher(script.html());

        	while (regMatch.find()) {
        		String key, csrName, csrValue, usernamekey, passwordkey;
        		key = regMatch.group(0);
        		csrName = regMatch.group(1);
        		csrValue = regMatch.group(2);
        		usernamekey = regMatch.group(3);
        		passwordkey = regMatch.group(4);
        		paramList.add(new Pair<>(csrName, csrValue));
        		paramList.add(new Pair<>(usernamekey, mUsername));
        		paramList.add(new Pair<>(passwordkey, mPassword));
        	}
        }

        return paramList;
    }

    public String getCookies() {
        return mShanbayCookies;
    }

    public void setCookies(String cookies) {
        this.mShanbayCookies = cookies;
    }

    public void login() throws Exception {
        CookieHandler.setDefault(mCookieManager);

        mPage = sendGet(mShanbayLogin);
        mPostParams = getFormParams(mPage, mUsername, mPassword);
        sendPost(mShanbayLogin, mPostParams);
    }
    
    private void getMembersPage() throws Exception {
    	mMemberPage = sendGet(mShanbayMemberPage);
    }
    
    /*
     *                 <tr class="member">
                    <td class="user">
                        <img src="https://static.baydn.com/avatar/media_store/560bc14fccae21eac89c827b5650945f.png?imageView/1/w/30/h/30/"  width="30" height="30" class="None"/> <a class="nickname" href="/checkin/user/15028954/">tianlanlan5889</a> <a class="level" href="/badger/user/tianlanlan0218/" title="成长值：17958"><span class="sun">&nbsp;</span> <span class="count">14</span></a> <a class="level" href="/badger/user/tianlanlan0218/" title="成长值：17958"><span class="moon">&nbsp;</span> <span class="count">1</span></a> <a class="level" href="/badger/user/tianlanlan0218/" title="成长值：17958"><span class="star">&nbsp;</span> <span class="count">4</span></a> 
                    </td>
                    <td class="points">18366</td>
                    <td class="days">438 天</td>
                    <td class="rate"><span class="">99.77&#37;</span></td>
                    <td class="checked ">
                        
                            <span class="label label-success">
                                已打卡
                            </span>
                        
                    </td>
                    <td class="operation">
                        <a href="/message/compose/?r=tianlanlan0218" target="_blank">提醒</a>
                    </td>
                </tr>
     */
    private void parseMembersPage() throws Exception {
    	//deal with mMemberPage
    	if (mMemberPage.isEmpty()) {
    		System.out.println("Member page is empty!");
    		return;
    	}
        Document doc = Jsoup.parse(mMemberPage);

        /* <table id="members" class="table table-bordered table-striped"> */
        Elements membersClass; //= doc.getElementsByClass("members");
        membersClass = doc.getElementsByTag("table");
        for (Element inputElement : membersClass) {          
            if (inputElement.id().equals("members") ) { /* table and its id is "members" is what member infor */
            	Elements members = inputElement.children();
            	/* travel all of the members in this members class */
            	/* each member has following:
            	 *  <thead>
                    <tr class="title">
                        <th class="user" width="">用户</th>
                        <th class="points" width="">贡献成长值</th>
                        <th class="days" width="">组龄</th>
                        <th class="rate" width="">打卡率</th>
                        <th class="checked" width="">今天已打卡</th>
                        <th class="operation" width="">操作</th>
                    </tr>csrftoken=0C2SD15TC27WGXHkH4sc4kT1mhMISuFo; Domain=.shanbay.com; expires=Sat, 30-Sep-2017 00:19:08 GMT; Max-Age=31449600; Path=/username=; expires=Thu, 01-Jan-1970 00:00:00 GMT; Max-Age=0; Path=/userid=; expires=Thu, 01-Jan-1970 00:00:00 GMT; Max-Age=0; Path=/SERVER_ID=e022a677-96be8784; path=/
                </thead>
            	 */
            	for (Element memberElement : members) {
            		Elements mElements = memberElement.getElementsByClass("member");
            		/* get each member's nickname, checked, ... */
            		for(Element e : mElements) {
            			Elements esNickname = e.getElementsByClass("nickname");
            			Elements checked = e.getElementsByClass("checked");
            			String nickname="", checked_str="";
            			String days, rate, points;
            			
            			nickname = esNickname.first().text();
            			checked_str = checked.first().text();
            			days = e.getElementsByClass("days").first().text();
            			points = e.getElementsByClass("points").first().text();
            			rate = e.getElementsByClass("rate").first().text();
            			if (!nickname.isEmpty()) {
            				ArrayList<String> userAttr = new ArrayList<String>();
            			
            				userAttr.add(points);
            				userAttr.add(days);
            				userAttr.add(rate);
            				userAttr.add(checked_str);
            				if (mMemberHash != null) {
            					mMemberHash.put(nickname, userAttr);
            				} else {
            					mMemberHash = new HashMap<String, ArrayList<String>>();
            					mMemberHash.put(nickname, userAttr);
            				}
            			}
            		}
            	}
            }
        }
        
        /* need to parse total pages of member informaiton */
        if (-1 == mMemberPageCnt) {
        	Elements ePageCnt;
        	
        	ePageCnt = doc.getElementsByClass("endless_page_link");
        	for(Element e : ePageCnt) {
        		Integer cnt;
        		try {
        			cnt = Integer.valueOf(e.text());
        		} catch (NumberFormatException ex) {
        			cnt = -1;
        		}
        		if (cnt.intValue() > mMemberPageCnt) {
        			mMemberPageCnt = cnt.intValue();
        		}
        	}
        	System.out.println("Total page of member information is " + mMemberPageCnt);
        }
        
    }
    
    public void getParseAllMemberInfo() throws Exception {
		/* get first page */
    	getMembersPage();
    	/* parse the first page and get the total number of pages:mMemberPageCnt */
		parseMembersPage();
		
		for(int i = 2; i <= mMemberPageCnt; i ++) {
    		mShanbayMemberPage = mShanbayUrlBase + "team/members/?page=" + i + "#p1";
    		
    		getMembersPage();
    		parseMembersPage();
    	}
    }

    public void generateNoneCheckedReport() {
        String sClipdata = null;

    	for(Map.Entry<String, ArrayList<String>> entry : mMemberHash.entrySet()) {
    		String check = entry.getValue().get(3);
    		if ( check.equals("未打卡") ) {
    			ShanbayDebug(4, entry.getKey() + " " + entry.getValue().get(3));
                if (sClipdata == null) {
                    sClipdata = "@".concat(entry.getKey());
                } else {
                    sClipdata = sClipdata + " @" + entry.getKey();
                }
    		}
    	}

        if (sClipdata != null) {
            sClipdata = sClipdata + " " + mWechatMessage;
            mMemberNoneCheckin = sClipdata;
        }
    }

    public String getMemberNoneCheckin()
    {
        return mMemberNoneCheckin;
    }
}
