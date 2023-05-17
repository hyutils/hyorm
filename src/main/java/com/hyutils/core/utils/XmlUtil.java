package com.hyutils.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class XmlUtil {

    /**
     * xml 转String
     *
     * @param file
     * @return
     * @throws DocumentException
     */
    public static String xml2String(File file) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(file);
        return document.asXML();
    }

    /**
     * xml 转String
     *
     * @param inputStream
     * @return
     * @throws DocumentException
     */
    public static String xml2String(InputStream inputStream) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(inputStream);
        return document.asXML();
    }

    /**
     * xml 转String
     *
     * @param url
     * @return
     * @throws DocumentException
     */
    public static String xml2String(URL url) {
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(url);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return document.asXML();
    }

    /**
     * String 转 org.dom4j.Document
     *
     * @param xml
     * @return
     * @throws DocumentException
     */
    private static Document strToDocument(String xml) throws DocumentException {
        return DocumentHelper.parseText(xml);
    }

    /**
     * xml 转  com.alibaba.fastjson.JSONObject
     *
     * @param xml
     * @return
     * @throws DocumentException
     */
    public static JSONObject documentToJSONObject(String xml) {
        JSONObject jsonObject = null;
        try {
            jsonObject = elementToJSONObject(strToDocument(xml).getRootElement());
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            return jsonObject;
        }
    }

    /**
     * org.dom4j.Element 转  com.alibaba.fastjson.JSONObject
     *
     * @param node
     * @return
     */
    public static JSONObject elementToJSONObject(Element node) {
        JSONObject result = new JSONObject();
        // 当前节点的名称、文本内容和属性
        List<Attribute> listAttr = node.attributes();// 当前节点的所有属性的list
        for (Attribute attr : listAttr) {// 遍历当前节点的所有属性
            result.put(attr.getName(), attr.getValue());
        }
        // 递归遍历当前节点所有的子节点
        List<Element> listElement = node.elements();// 所有一级子节点的list
        if (!listElement.isEmpty()) {
            for (Element e : listElement) {// 遍历所有一级子节点
                if (e.attributes().isEmpty() && e.elements().isEmpty()) // 判断一级节点是否有属性和子节点
                {
                    result.put(e.getName(), e.getTextTrim());// 沒有则将当前节点作为上级节点的属性对待
                } else {
                    if (!result.containsKey(e.getName())) // 判断父节点是否存在该一级节点名称的属性
                    {
                        result.put(e.getName(), new JSONArray());// 没有则创建
                    }
                    ((JSONArray) result.get(e.getName())).add(elementToJSONObject(e));// 将该一级节点放入该节点名称的属性对应的值中
                }
            }
        }
        return result;
    }

    /**
     * xml 转java对象
     *
     * @param xml
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T xml2Object(String xml, Class<T> t) {
        try {
            return JSON.toJavaObject(documentToJSONObject(xml), t);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * xml 转java对象
     *
     * @param element
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T xml2Object(Element element, Class<T> t) {
        try {
            return JSON.toJavaObject(elementToJSONObject(element), t);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * xml 转java对象
     *
     * @param url
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T xml2Object(URL url, Class<T> t) {
        try {
            return JSON.toJavaObject(documentToJSONObject(xml2String(url)), t);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * xml 转java对象
     *
     * @param file
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T xml2Object(File file, Class<T> t) {
        try {
            return JSON.toJavaObject(documentToJSONObject(xml2String(file)), t);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * xml 转java对象
     *
     * @param inputStream
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T xml2Object(InputStream inputStream, Class<T> t) {
        try {
            return JSON.toJavaObject(documentToJSONObject(xml2String(inputStream)), t);
        } catch (Exception e) {
            return null;
        }
    }
}
