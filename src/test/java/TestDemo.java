import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by zhengcong on 2017/4/14.
 */
public class TestDemo {

    @Test
    public void test1() {

        try {
            Map<String, Integer> result = new TreeMap<String, Integer>();
            Document doc = Jsoup.connect("http://mvnrepository.com/artifact/junit/junit").get();
            Elements es = doc.getElementsByClass("vbtn release");
            for (Element e : es) {
                String version = e.attr("href").substring(6);
                String desUrl = "junit/" + version + "/usages";
                Element as = doc.select("a[href=" + desUrl + "]").first();
                String temp = as.text().replaceAll(",", "");
                result.put(version, Integer.valueOf(temp));

            }
            List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(result.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
            FileWriter fw = new FileWriter(new File("test.txt"));
            for (Map.Entry<String, Integer> m : list) {
                System.out.printf("版本号【%s】使用量为 %d",m.getKey(),m.getValue());
                System.out.println();
                fw.write("版本号【"+m.getKey()+"】使用量为 "+m.getValue()+"\n");
                fw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test2(){

        System.setProperty("webdriver.chrome.driver", "/Users/dasouche/Downloads/chromedriver 2");

        WebDriver webDriver = new ChromeDriver();

        webDriver.get("https://www.douban.com/");


        WebElement we = webDriver.findElement(By.id("form_email"));

        we.sendKeys("cong99299618@hotmail.com");

        WebElement we2 = webDriver.findElement(By.id("form_password"));

        we2.sendKeys("W#6erm9");

        WebElement sub = webDriver.findElement(By.className("bn-submit"));

        sub.click();

        Set<Cookie> cookie = webDriver.manage().getCookies();

        Map<String,String> cookies = new HashMap<String, String>();

        for(Cookie c:cookie){

            cookies.put(c.getName(),c.getValue());

        }

        System.out.println(cookies.toString());

        webDriver.quit();

    }

}
