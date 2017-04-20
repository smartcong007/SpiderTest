package main;

import Model.Movie;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by zhengcong on 2017/4/14.
 */
public class Douban250Spider {

    private static LinkedList<String> resolvedList;   //存放待解析网页路径的队列

    private static List<String> failedList = new ArrayList<>();  //存放抓取失败的网页路径

    private static final String homeUrl = "https://movie.douban.com/top250";   //给定初始页

    private static FileWriter fw;

    private static Map<String,String> cookies;     //cookies用于对付豆瓣的反爬


    public Douban250Spider(String filepath,String accout,String pass){

        init(filepath,accout,pass);

    }

    public void init(String filepath,String accout,String pass){

        //初始化待抓取网页链接队列
        resolvedList = new LinkedList<String>();
        for(int i = 0; i < 10; i ++){
            resolvedList.add(homeUrl+"?start="+i*25+"&filter=");
        }

        //初始化本地存储抓取结果的文件路径
        try {
            fw = new FileWriter(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //初始化cookies,需要调用方提供自己在豆瓣的账号密码
        if(cookies.size() == 0){

            autoSetCookies(accout,pass);

        }

    }

    public static void crawlwe(String url){    //对给定链接的网页进行解析，获取需要的部分

        try {
            Document doc = Jsoup.connect(url).cookies(cookies)       //为了避免豆瓣对反复爬取网页行为的ip进行拦截，所以借助cookie冒充登录用户即可，有待优化
                    .get();


            if(url.contains(homeUrl)){

                Elements elements = doc.select("div.hd > a");

                for(Element e:elements){

                    resolvedList.add(e.attr("href"));

                }

            }else{

                Movie movie = new Movie();

                String title = doc.select("span[property=v:itemreviewed]").text();

                String headImg = doc.select("img[title=点击看更多海报]").attr("src");

                String directer = doc.select("a[rel=v:directedBy]").text();

                StringBuffer actors = new StringBuffer();

                Elements es = doc.select("a[rel=v:starring]");

                for(Element e:es){

                    actors.append(e.text()).append("/");

                }

                StringBuffer script = new StringBuffer();

                Elements span = doc.select("span.attrs > a");

                for(Element ee:span){

                    if(ee.attr("rel").contains("v:")){

                        continue;
                    }else{

                        script.append(ee.text()).append("/");

                    }

                }

                StringBuffer classify = new StringBuffer();

                Elements cla = doc.select("span[property=v:genre]");
                for(Element eee:cla){
                    classify.append(eee.text()).append("/");
                }

                StringBuffer date = new StringBuffer();

                Elements da = doc.select("span[property=v:initialReleaseDate]");

                for(Element e3:da){
                    date.append(e3.text()).append("/");
                }

                String time = doc.select("span[property=v:runtime]").text();
                String score = doc.select("strong[property=v:average]").text();
                String content = doc.select("span[property=v:summary]").text().trim().substring(0,50)+"...";


                movie.setActors(actors.toString());
                movie.setCastDate(date.toString());
                movie.setClassify(classify.toString());
                movie.setContent(content);
                movie.setDirecter(directer);
                movie.setHeadImg(headImg);
                movie.setScore(score);
                movie.setScriptBy(script.toString());
                movie.setTime(time);
                movie.setTitle(title);

                String json = JSONObject.toJSONString(movie);
                System.out.println(json);
                fw.write(json+"\n");
                fw.flush();

            }
        } catch (IOException e) {

            String error = e.toString();
            if(error.contains("Status=404")){

                failedList.add(error.substring(error.indexOf("URL=")+4));  //记录下可能抓取失败的电影详情页

            }

        }

    }

    public static void autoSetCookies(String account,String pass){

        System.out.println("****** automatically get your cookies from douban! ******");

        System.setProperty("webdriver.chrome.driver", "/Users/dasouche/Downloads/chromedriver 2");

        WebDriver webDriver = new ChromeDriver();

        webDriver.get("https://www.douban.com/");


        WebElement we = webDriver.findElement(By.id("form_email"));

        we.sendKeys(account);

        WebElement we2 = webDriver.findElement(By.id("form_password"));

        we2.sendKeys(pass);

        WebElement sub = webDriver.findElement(By.className("bn-submit"));

        sub.click();

        Set<Cookie> cookie = webDriver.manage().getCookies();

        Map<String,String> cookies = new HashMap<String, String>();

        for(Cookie c:cookie){

            cookies.put(c.getName(),c.getValue());

        }

        System.out.println("cookies :"+cookies.toString());

        webDriver.quit();

    }

    public void spider() {

        System.out.println("spider beginning.......");

        while (resolvedList.size()>0){
            String url = resolvedList.poll();
            crawlwe(url);
        }

        System.out.println("spider finished......");

        if(failedList.size()>0){

            System.out.println("The failed items are listed below:");
            for(String s:failedList){

                System.out.println(s);

            }

        }

    }

}
