package main;

import Model.Movie;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by zhengcong on 2017/4/14.
 */
public class Douban250Spider {

    private static LinkedList<String> resolvedList;   //存放待解析网页路径的队列

    private static final String homeUrl = "https://movie.douban.com/top250";   //给定初始页

    private static FileWriter fw;

    private static Map<String,String> cookies;

    private static  final String cookie = "ll=\"118172\"; bid=gfFuWV33eMI; ps=y; ue=\"cong99299618@hotmail.com\"; dbcl2=\"76578935:RBoNAdqQqyQ\"; ck=RJkR; ap=1; push_noty_num=0; push_doumail_num=0; _vwo_uuid_v2=2AB55A255694C691F4AC27222082FCBF|6894aba1ec2b204bebe44d712191ce96; __utma=30149280.1026275788.1492169340.1492169340.1492174928.2; __utmb=30149280.3.10.1492174928; __utmc=30149280; __utmz=30149280.1492169340.1.1.utmcsr=baidu|utmccn=(organic)|utmcmd=organic";

    static {

        resolvedList = new LinkedList<String>();
        for(int i = 0; i < 10; i ++){
            resolvedList.add(homeUrl+"?start="+i*25+"&filter=");
        }

        try {
            fw = new FileWriter("douban.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        cookies = new HashMap<String, String>();
        String s[] = cookie.split("; ");
        for(String s1:s){

            String temp[] = s1.split("=");
            cookies.put(temp[0],temp[1]);

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
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        while (resolvedList.size()>0){
            String url = resolvedList.poll();
            crawlwe(url);
        }

    }

}
