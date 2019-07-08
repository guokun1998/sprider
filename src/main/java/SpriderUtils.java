import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.cert.X509Certificate;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author guokun
 * @create 2019-07-05-16:43
 */
public class SpriderUtils {
    /**
     * 创建客户端
     * @param cookieStore
     * @return
     * @throws Exception
     */
    public static CloseableHttpClient createHttpClientWithNoSsl(CookieStore cookieStore) throws Exception {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // don't check
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // don't check
                    }
                }
        };

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAllCerts, null);
        LayeredConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(ctx);
        return HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .setDefaultCookieStore(cookieStore == null ? new BasicCookieStore() : cookieStore)
                .build();
    }

    public static String readResponse(HttpResponse response) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        String result = new String();
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        return result;
    }

    public static Document getDocument(CloseableHttpClient client, String url,String jsessionid) throws IOException {
        HttpGet request = new HttpGet(url);
        request.setHeader("Cookie",jsessionid);
        HttpResponse response = client.execute(request);
        return Jsoup.parse(SpriderUtils.readResponse(response));
    }

    /**
     * 退出
     * @param client
     * @param jsessionid
     * @throws IOException
     */
    public static void logout(CloseableHttpClient client, String jsessionid) throws IOException {
        getDocument(client, "http://edusys.hrbeu.edu.cn/jsxsd/xk/LoginToXk?method=exit&tktime=1562563722000",jsessionid);
    }

    /**
     * 登录，从配置文件中读取账号密码
     * @param client
     * @return
     * @throws Exception
     */
    public static LoginInformation login(CloseableHttpClient client) throws Exception {
        Properties properties = new Properties();
        try (InputStream is = SpriderUtils.class.getResourceAsStream("/login.properties")) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        return login(client,username,password);

    }

    /**
     * 登录，直接输入账号密码
     * @param client
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    public static LoginInformation login(CloseableHttpClient client, String username, String password) throws Exception {


        LoginInformation loginInformation = getRequest1(client);
        LoginInformation loginInformation1 = loginPostRequest1(client, loginInformation, username, password);


        return loginInformation1;
    }

    /**
     * 查询成绩
     * @param client
     * @param kksj 学期 ""就是所有，部分例如"2018-2019-2"
     * @param jsessionid
     * @return
     * @throws IOException
     */
    public static Document examPostRequstion(CloseableHttpClient client, String kksj, String jsessionid) throws IOException {
        HttpGet httpGet = new HttpGet("http://edusys.hrbeu.edu.cn/jsxsd/kscj/cjcx_query.do?Ves632DSdyV=NEW_XSD_XJCJ");

        HttpPost httpPost = new HttpPost("http://edusys.hrbeu.edu.cn/jsxsd/kscj/cjcx_list");
        httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
        httpPost.setHeader("Cookie",jsessionid);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("kksj", kksj));
        params.add(new BasicNameValuePair("kcxz", ""));
        params.add(new BasicNameValuePair("kcmc", ""));
        params.add(new BasicNameValuePair("xsfs", "all"));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        client.execute(httpGet);
        CloseableHttpResponse response = client.execute(httpPost);
        return Jsoup.parse(SpriderUtils.readResponse(response));


    }

    /**
     * 查询课表全部
     * @param client
     * @param jsessionid
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static Document getTimetable(CloseableHttpClient client, String jsessionid) throws IOException, InterruptedException {
        HttpGet request = new HttpGet("http://edusys.hrbeu.edu.cn/jsxsd/xskb/xskb_list.do?Ves632DSdyV=NEW_XSD_PYGL");
        request.setHeader("Cookie",jsessionid);
        client.execute(request);
        HttpResponse response = client.execute(request);
        return Jsoup.parse(SpriderUtils.readResponse(response));
    }

    /**
     * 查询小题分
     * @param client
     * @param url
     * @param jsessionid
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static Document getExamInformation(CloseableHttpClient client, String url, String jsessionid) throws IOException, InterruptedException {
        HttpGet request = new HttpGet(url);
        request.setHeader("Cookie",jsessionid);
        client.execute(request);
        HttpResponse response = client.execute(request);
        return Jsoup.parse(SpriderUtils.readResponse(response));
    }


    public static class LoginInformation{
        private String jsessionid;
        private String lt;
        private String CASTGC;

        public LoginInformation(String jsessionid, String lt) {
            this.jsessionid = jsessionid;
            this.lt = lt;
        }

        public LoginInformation(String jsessionid, String lt, String CASTGC) {
            this.jsessionid = jsessionid;
            this.lt = lt;
            this.CASTGC = CASTGC;
        }

        public String getJsessionid() {
            return jsessionid;
        }

        public void setJsessionid(String jsessionid) {
            this.jsessionid = jsessionid;
        }

        public String getLt() {
            return lt;
        }

        public void setLt(String lt) {
            this.lt = lt;
        }

        public String getCASTGC() {
            return CASTGC;
        }

        public void setCASTGC(String CASTGC) {
            this.CASTGC = CASTGC;
        }
    }



    /**
     * 登录的第一步
     * @param client
     * @return
     * @throws IOException
     */
    private static LoginInformation getRequest1(CloseableHttpClient client) throws IOException {
        /* 第一次请求[GET] 拉取流水号信息 */
        HttpGet request = new HttpGet("https://cas.hrbeu.edu.cn/cas/login");
        HttpResponse response = client.execute(request);
        /*
        从cookie中取出jsessionid
         */
        Header headerSetCookie = response.getFirstHeader("Set-Cookie");
        String TGC = headerSetCookie.getValue();
        int indexOfJ = TGC.indexOf(";");
        String jsessionid = TGC.substring(0,indexOfJ);

        /*
        从response得到htmlPage
         */
        Document htmlPage = Jsoup.parse(readResponse(response));
        Element form = htmlPage.select("#fm1").first();
        String lt = form.select("[name=lt]").first().val();
        LoginInformation loginInformation = new LoginInformation(jsessionid,lt);
        return loginInformation;
    }

    /**
     * 登录的第二步
     * @param client
     * @param loginInformation
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    private static LoginInformation loginPostRequest1(CloseableHttpClient client, LoginInformation loginInformation, String username, String password) throws IOException {
        /* 第二次请求[POST] 发送表单验证信息 */
        HttpPost request2 = new HttpPost("https://cas.hrbeu.edu.cn/cas/login;"+loginInformation.getJsessionid());
        request2.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
        request2.setHeader("Cookie",loginInformation.getJsessionid()+"; MESSAGE_TICKET=%7B%22times%22%3A0%7D");//%7B%22times%22%3A0%7D
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("captcha", ""));
        params.add(new BasicNameValuePair("lt", loginInformation.lt));
        params.add(new BasicNameValuePair("execution", "e1s1"));
        params.add(new BasicNameValuePair("_eventId", "submit"));
        params.add(new BasicNameValuePair("submit", "登 录"));
        request2.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response2 = client.execute(request2);

        Header[] headers = response2.getHeaders("Set-Cookie");
        String CASTGCorg = headers[1].getValue();
        int indexOfD = CASTGCorg.indexOf("=");
        int indexOfF = CASTGCorg.indexOf(";");
        String CASTGC = CASTGCorg.substring(indexOfD+1,indexOfF);
        loginInformation.setCASTGC(CASTGC);
        return loginInformation;
    }

}
