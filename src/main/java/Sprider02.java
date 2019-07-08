import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * @author guokun
 * @create 2019-07-08-15:08
 */
public class Sprider02 {
    public static void main(String[] args) throws IOException {
        Sprider02 sprider02 = new Sprider02();
//        sprider02.getAll("http://edusys.hrbeu.edu.cn/jsxsd/kscj/pscj_list.do?xs0101id=2016201306&jx0404id=201820192004124&&cj0708id=D58F82C600D347E6B057DCC9C4D33807","管理学",2016201301,2016201330,new GuanlixueCaculateWay());
//        sprider02.getAll("http://edusys.hrbeu.edu.cn/jsxsd/kscj/pscj_list.do?xs0101id=2016201306&jx0404id=201820192007478&&cj0708id=78B8F7978EF941359C0EB8DB65C633B4","英语",2016201301,2016201330,new EnglishCaculateWay());
        sprider02.getAll("http://edusys.hrbeu.edu.cn/jsxsd/kscj/pscj_list.do?xs0101id=2016201306&jx0404id=201820192004015&&cj0708id=1E6D717D04B34CFEBDE6A38BA81BFD87","毛概",2016201301,2016201330,new EnglishCaculateWay());
    }

    public void getAll(String url, String className, int startNo, int endNo, ExamCaculateWay examCaculateWay) throws IOException {
        CloseableHttpClient client = null;
        SpriderUtils.LoginInformation loginInformation = null;
        try {
            CookieStore httpCookieStore = new BasicCookieStore();
            client = SpriderUtils.createHttpClientWithNoSsl(httpCookieStore);
            loginInformation = SpriderUtils.login(client);

            int indexOf = url.indexOf("2016201306");
            String url1 = url.substring(0, indexOf);
            String url2 = url.substring(indexOf + 10);

            System.out.println("----------------"+className+"---------------------");
            for (int no = startNo; no <= endNo; no++) {
                Document document;
                if (no == startNo) {
                    document = SpriderUtils.getExamInformation(client,url1+no+url2,loginInformation.getJsessionid());

                }
                else {
                    document = SpriderUtils.getDocument(client,url1+no+url2,loginInformation.getJsessionid());
                }
                Elements tds = document.getElementsByTag("td");
                double sum=0;
                for (int i = 1; i < tds.size()-1; i+=2) {
                    Element element = tds.get(i);
                    String s1 = element.childNode(0).toString();
                    String fen = s1.substring(0, s1.length() - 1);
                    double fe = Double.valueOf(fen);
                    fe *= 0.01;
                    double num = Double.valueOf(tds.get(i + 1).childNode(0).toString());
                    sum += examCaculateWay.caculate(fe,num,sum);

                }

                System.out.println(no+"分数:"+sum);
            }
            System.out.println("---------------------------------------------------");


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null && loginInformation != null) {
                SpriderUtils.logout(client,loginInformation.getJsessionid());
            }
        }
    }

}
