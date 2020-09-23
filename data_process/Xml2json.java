package com.yudao.one;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.dom4j.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Xml2json {

    public static void main(String[] args) throws Exception {
        dealFile("/Users/wangguangzhi/Documents/workspace/github/text-classification/data/news_sohusite_xml.dat");
        //Document doc= DocumentHelper.parseText(xmlStr);
        //JSONObject json=new JSONObject();
        //dom4j2Json(doc.getRootElement(),json);
        //System.out.println("xml2Json:"+json.toJSONString());

        /*
        String xml = "<doc>\n" +
                "<url>http://club.baobao.sohu.com/read_elite.php?b=mom_daugh&a=12455341</url>\n" +
                "<docno>4a22da9a76ac0ed3-e4113306c0bb3300</docno>\n" +
                "<contenttitle>主题：不管孩子多少，只要有一对无耻的儿子媳妇，老人就完蛋</contenttitle>\n" +
                "<content>这次回老家，去看姑姑。姑姑老的不成样子，身体极差，姑父身体还行，就是因为脑袋做过手术，说话有点稀里糊涂的。\uE40C姑姑有４个孩子，有三个生活非常不错。但有一个儿子，生活很不好，这个儿子和我还是同学。特别是这个儿子的媳妇，那是超级的愣货，我姑姑以前那么要强的人，现在被这个儿子和媳妇逼的无路可走。</content>\n" +
                "</doc>";
        */
        //System.out.println(xml2Json(xml).toJSONString());
    }

    static List<File> fileList = new ArrayList<>();

    static {
        for(int i = 1; i< 16; i++) {
            fileList.add(new File("/Users/wangguangzhi/Documents/workspace/github/text-classification/data/news_sohusite_"+i+".json"));
        }
    }

    public static void dealFile(String path) throws Exception {
        File file = new File(path);
        BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis,"gbk"),5*1024*1024);// 用5M的缓冲读取文本文件
        StringBuffer doc = new StringBuffer();
        String line = "";
        int count = 1;
        while((line = reader.readLine()) != null) {
            int doc_num = count / 100000;
            if(count % 100000 == 1) {
                FileUtils.writeStringToFile(fileList.get(doc_num), "[\r\n", "UTF-8",true);
            }
            doc.append(line);
            if("</doc>".equals(line)) {
                JSONObject json = xml2Json(doc.toString());
                FileUtils.writeStringToFile(fileList.get(doc_num), json.toJSONString() + ",\r\n", "UTF-8",true);
                doc.delete(0,doc.length());
                count++;
            }
            if(count % 100000 == 0) {
                FileUtils.writeStringToFile(fileList.get(doc_num), "{\"contenttitle\":\"\",\"docno\":\"\",\"url\":\"\",\"content\":\"\"}\r\n", "UTF-8",true);
                FileUtils.writeStringToFile(fileList.get(doc_num), "]\r\n", "UTF-8",true);
            }
        }

    }

    private static String getOutPutFileName(int count){
        return (count / 100000) + "";
    }

    /**
     * xml转json
     * @param xmlStr
     * @return
     * @throws DocumentException
     */
    public static JSONObject xml2Json(String xmlStr) throws DocumentException {
        Document doc= DocumentHelper.parseText(xmlStr.replace("&", "&amp;"));
        JSONObject json=new JSONObject();
        dom4j2Json(doc.getRootElement(), json);
        return json;
    }

    /**
     * xml转json
     * @param element
     * @param json
     */
    public static void dom4j2Json(Element element, JSONObject json){
        //如果是属性
        for(Object o:element.attributes()){
            Attribute attr=(Attribute)o;
            if(!isEmpty(attr.getValue())){
                json.put("@"+attr.getName(), attr.getValue());
            }
        }
        List<Element> chdEl=element.elements();
        if(chdEl.isEmpty()&&!isEmpty(element.getText())){//如果没有子元素,只有一个值
            json.put(element.getName(), element.getText());
        }

        for(Element e:chdEl){//有子元素
            if(!e.elements().isEmpty()){//子元素也有子元素
                JSONObject chdjson=new JSONObject();
                dom4j2Json(e,chdjson);
                Object o=json.get(e.getName());
                if(o!=null){
                    JSONArray jsona=null;
                    if(o instanceof JSONObject){//如果此元素已存在,则转为jsonArray
                        JSONObject jsono=(JSONObject)o;
                        json.remove(e.getName());
                        jsona=new JSONArray();
                        jsona.add(jsono);
                        jsona.add(chdjson);
                    }
                    if(o instanceof JSONArray){
                        jsona=(JSONArray)o;
                        jsona.add(chdjson);
                    }
                    json.put(e.getName(), jsona);
                }else{
                    if(!chdjson.isEmpty()){
                        json.put(e.getName(), chdjson);
                    }
                }


            }else{//子元素没有子元素
                for(Object o:element.attributes()){
                    Attribute attr=(Attribute)o;
                    if(!isEmpty(attr.getValue())){
                        json.put("@"+attr.getName(), attr.getValue());
                    }
                }
                if(!e.getText().isEmpty()){
                    json.put(e.getName(), e.getText());
                }
            }
        }
    }

    public static boolean isEmpty(String str) {

        if (str == null || str.trim().isEmpty() || "null".equals(str)) {
            return true;
        }
        return false;
    }

}
