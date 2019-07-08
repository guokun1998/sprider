import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author guokun
 * @create 2019-07-05-16:42
 */
public class Sprider01 {
    public static void main(String[] args) throws Exception {
        CookieStore httpCookieStore = new BasicCookieStore();
        CloseableHttpClient client = SpriderUtils.createHttpClientWithNoSsl(httpCookieStore);
        SpriderUtils.LoginInformation loginInformation = SpriderUtils.login(client);
        Document document = SpriderUtils.getTimetable(client, loginInformation.getJsessionid());
        System.out.println(document);

        SpriderUtils.logout(client,loginInformation.getJsessionid());
    }


}
