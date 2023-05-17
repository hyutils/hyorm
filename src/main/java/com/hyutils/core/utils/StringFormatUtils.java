package com.hyutils.core.utils;

import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFormatUtils {

    public static String snake(String x, Boolean big) {
        Boolean up = big;
        String y = "";
        for (int i = 0; i < x.length(); i++) {
            if (up) {
                String tmp = "";
                tmp = tmp + x.charAt(i);
                y = y + tmp.toUpperCase();
                up = false;
            } else if (x.charAt(i) == '_') {
                up = true;
            } else {
                y = y + x.charAt(i);
            }
        }
        return y;
    }


    public static Boolean isUpper(String x) {
        if (!StringUtils.hasText(x)) {
            return false;
        }
        if (x.toUpperCase().equals(x)) {
            return true;
        }
        return false;
    }

    public static String camel(String x) {
        if (x.contains("_")) return x;
        String y = "";
        for (int i = 0; i < x.length(); i++) {
            if (isUpper(x.charAt(i) + "")) {
                if (y.equals("")) {
                    y = (x.charAt(i) + "").toLowerCase();
                } else {
                    y = y + "_" + (x.charAt(i) + "").toLowerCase();
                }
            } else {
                y = y + x.charAt(i);
            }
        }
        return y;
    }

    public static String formatByName(String source, Map<String, Object> params) {
        Pattern pattern = Pattern.compile("\\{[a-zA-Z_:]+\\}");
        Matcher matcher = pattern.matcher(source);
        List<String> tmp = new ArrayList<>();
        while (matcher.find()) {
//            System.out.println(matcher.group());
            tmp.add(matcher.group());
        }
        for (String x : tmp) {
            String y = x.replace("{", "").replace("}", "");
            if (Objects.nonNull(params.get(y))) {
                source = source.replace(x, params.get(y).toString());
            }
        }
        return source;
    }

    public static String getStrOfStream(InputStream is){
        byte[] byteArr = new byte[0];
        try {
            byteArr = new byte[is.available()];
            is.read(byteArr);
            String str = new String(byteArr);
            return str;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertInputStreamToString(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[64];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    public static List<String> getFormatNames(String source) {
        Pattern pattern = Pattern.compile("\\{[a-zA-Z_0-9:]+\\}");
        Matcher matcher = pattern.matcher(source);
        List<String> tmp = new ArrayList<>();
        while (matcher.find()) {
//            System.out.println(matcher.group());
            tmp.add(matcher.group().replace("{", "").replace("}", ""));
        }
        return tmp;
    }

    public static String strGetOrDefault() {
        return "String {key} = (String)tmp.getOrDefault(\"{key}\",\"\");";
    }

    public static String intGetOrDefault() {
        return "Integer {key} = (Integer)tmp.getOrDefault(\"{key}\", -1);";
    }

    public static String mapGetOrDefault() {
        return "Map<String,Object> {key} = (Map<String,Object>)tmp.getOrDefault(\"{key}\", new HashMap<>());";
    }

    public static String listGetOrDefault() {
        return "List<String> {key} = (List<String>)tmp.getOrDefault(\"{key}\", new ArrayList<>());";
    }

    public static String booleanGetOrDefault() {
        return "Boolean {key} = (Boolean)tmp.getOrDefault(\"{key}\", false);";
    }

    public static String longGetOrDefault() {
        return "Long {key} = (Long)tmp.getOrDefault(\"{key}\", -1);";
    }

    public static String doubleGetOrDefault() {
        return "Double {key} = (Double)tmp.getOrDefault(\"{key}\", 0.0);";
    }

    public static String nullGetOrDefault() {
        return "Object {key} = (Object)tmp.getOrDefault(\"{key}\", \"\");";
    }

    public static String getField() {
        return "public {type} {key}; // {value}";
    }

    public static String getExchange() {
        return "tmpDto.{keySnake} = ({type})tmp.getOrDefault(\"{key}\", {default});";
    }

    public static String getListExchange() {
        return "try{\n" +
                "tmpDto.{keySnake} = new ArrayList<>();\n" +
                "List<Map<String, Object>> tmp{keySnakeBig} = (List<Map<String, Object>>) tmp.getOrDefault(\"{key}\", {default});\n" +
                "for (Map<String, Object> a : tmp{keySnakeBig}) {\n" +
                "    tmpDto.{keySnake}.add(exchange{currentName}{keySnakeBig}(a));\n" +
                "}\n" +
                "}catch (Exception e){\n" +
                "        }\n";
    }

    public static String getMapExchange() {
        return "tmpDto.{keySnake} = exchange{currentName}{keySnakeBig}(({type})tmp.getOrDefault(\"{key}\", {default}));";
    }

    public static String generateDto(Map<String, Object> tmp, String currentName, Set<String> names) {
//        tmp = removeNull(tmp);
        Set<String> x = tmp.keySet();
        StringBuilder ans = new StringBuilder();
        for (String y : x) {
            Map<String, Object> params = new HashMap<>();
            params.put("key", snake(y, false));
            params.put("value", tmp.get(y).toString());
            if (tmp.get(y) instanceof String) {
                params.put("type", "String");
            } else if (tmp.get(y) instanceof Integer) {
                params.put("type", "Integer");
            } else if (tmp.get(y) instanceof Map) {
                String tmpName = currentName + snake(y, true);
                if (names.contains(tmpName)) {
                    params.put("type", tmpName);
                } else {
                    params.put("type", "Map<String,Object>"); // TODO: 2022/5/9 需要考虑这里写入实际的类型
                }
            } else if (tmp.get(y) instanceof List) {
                String tmpName = currentName + snake(y, true);
                if (names.contains(tmpName)) {
                    params.put("type", "List<" + tmpName + ">");
                } else {
                    params.put("type", "List<String>"); // TODO: 2022/5/9 这里的字符串很有可能是一个对象类型
                }
            } else if (tmp.get(y) instanceof Boolean) {
                params.put("type", "Boolean");
            } else if (tmp.get(y) instanceof Long) {
                params.put("type", "Long");
            } else if (tmp.get(y) instanceof Float || tmp.get(y) instanceof Double) {
                params.put("type", "Double");
            } else {
//                System.out.println(y + "不存在" + tmp.get(y));
                params.put("type", "Object");
            }
            ans.append(StringFormatUtils.formatByName(getField(), params)).append("\n");
        }
        return ans.toString();
    }

    private static Map<String, Object> defaultValue = new HashMap<String, Object>() {
        {
            put("String", "\"\"");
            put("Integer", -1);
            put("Map<String,Object>", "new HashMap<>()");
            put("List<String>", "new ArrayList<>()");
            put("Boolean", false);
            put("Long", -1L);
            put("Double", 0.0);
        }
    };

    public static Map<String, Object> removeNull(Map<String, Object> tmp) {
        Map<String, Object> ans = new HashMap<>();
        for (Map.Entry<String, Object> x : tmp.entrySet()) {
            if (Objects.nonNull(x.getValue())) {
                if (x.getValue() instanceof Map) {
                    x.setValue(removeNull((Map<String, Object>) x.getValue()));
                }
                if (x.getValue() instanceof List) {
                    List<Map<String, Object>> kkk = new ArrayList<>();
                    List xx = (List) x.getValue();
                    if (xx.size() > 0) {
                        if (xx.get(0) instanceof Map) {
                            List<Map<String, Object>> xxx = xx;
                            for (Map<String, Object> xxxx : xxx) {
                                kkk.add(removeNull(xxxx));
                            }
                        }
                        x.setValue(kkk);
                    }
                }
                ans.put(x.getKey(), x.getValue());
            }
        }
        return ans;
    }

    public static Map<String, Object> recursionRemoveNull(Map<String, Object> tmp) {
        Map<String, Object> ans = new HashMap<>();
        for (Map.Entry<String, Object> x : tmp.entrySet()) {
            if (Objects.isNull(x.getValue())) {
                continue;
            }
            if (x.getValue() instanceof Map) {
                ans.put(x.getKey(), recursionRemoveNull((Map<String, Object>) x.getValue()));
            } else if (x.getValue() instanceof List) {
                List tmpAns = new ArrayList();
                try {
                    List<Map<String, Object>> y = (List<Map<String, Object>>) x.getValue();
                    for (Map<String, Object> z : y) {
                        tmpAns.add(recursionRemoveNull(z));
                    }
                } catch (Exception e) {
                    tmpAns.addAll((List) x.getValue());
//                    e.printStackTrace();
                }
                ans.put(x.getKey(), tmpAns);
            } else {
                ans.put(x.getKey(), x.getValue());
            }
        }
        return ans;
    }


    public static void getExchangeFromDto(String fileName, String dtoName) {
        String init = StringFormatUtils.formatByName("public static {dtoName} exchange{dtoName}(Map<String, Object> tmp) {\n{dtoName} tmpDto = new {dtoName}();", new HashMap<String, Object>() {
            {
                put("dtoName", dtoName);
            }
        });
        System.out.println(init);
        File file = new File(fileName);
        if (file.exists()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("public") && !line.contains("class")) {
                        String[] params = line.split(" ");
                        if (params.length >= 3) {
                            String param = params[2];
                            param = param.substring(0, param.length() - 1);
                            Map<String, Object> params1 = new HashMap<>();
                            if (params[1].equals("List<String>")) {
                                params1.put("type", "List<Map<String,Object>>");
                            } else {
                                params1.put("type", params[1]);
                            }
                            params1.put("key", camel(param));
                            params1.put("keySnake", param);
                            params1.put("default", defaultValue.get(params[1]));
                            System.out.println(StringFormatUtils.formatByName(getExchange(), params1));
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("return tmpDto;\n}");
    }


    public static Map<String, Object> getKeyTypeFromMap(Map<String, Object> tmp) {
        Map<String, Object> ans = new HashMap<>();
        for (Map.Entry<String, Object> a : tmp.entrySet()) {
            if (a.getValue() instanceof String) {
                // TODO: 2021/11/13 还需要区分特殊的，比如时间
                if (DatetimeUtil.getTimeStampOfViewStr2((String) a.getValue()) != 0L) {
                    ans.put(a.getKey(), "timestamp");
                } else {
                    ans.put(a.getKey(), "string");
                }
            } else if (a.getValue() instanceof Integer) {
                ans.put(a.getKey(), "int");
            } else if (a.getValue() instanceof Long) {
                ans.put(a.getKey(), "long");
            } else if (a.getValue() instanceof Map) {
                ans.put(a.getKey(), "map");
            } else if (a.getValue() instanceof List) {
                ans.put(a.getKey(), "list");
            } else if (a.getValue() instanceof Boolean) {
                ans.put(a.getKey(), "bool");
            } else if (a.getValue() instanceof Float || a.getValue() instanceof Double) {
                ans.put(a.getKey(), "double");
            } else {
                ans.put(a.getKey(), a.getValue());
            }
        }
        return ans;
    }


    public static String getExchangeFromMap(Map<String, Object> tmp, String dtoName) {
//        tmp = removeNull(tmp);
        String init = StringFormatUtils.formatByName("public static {dtoName} exchange{dtoName}(Map<String, Object> tmp) {\n{dtoName} tmpDto = new {dtoName}();", new HashMap<String, Object>() {
            {
                put("dtoName", dtoName);
            }
        });
        StringBuilder ans = new StringBuilder();
        ans.append(init).append("\n");
        Set<String> x = tmp.keySet();
        for (String y : x) {
            Map<String, Object> params = new HashMap<>();
            params.put("keySnake", snake(y, false));
            params.put("keySnakeBig", snake(y, true));
            params.put("currentName", dtoName);
            params.put("key", y);
            int listFlag = 0;
            int mapFlag = 0;
            if (tmp.get(y) instanceof String) {
                params.put("type", "String");
                params.put("default", "\"\"");
            } else if (tmp.get(y) instanceof Integer) {
                params.put("type", "Integer");
                params.put("default", -1);
            } else if (tmp.get(y) instanceof Map) {
                params.put("type", "Map<String,Object>");
                params.put("default", "new HashMap<>()");
                mapFlag = 1;
            } else if (tmp.get(y) instanceof List) {
                List z = (List) tmp.get(y);
                int flag = 0;
                if (z.size() > 0) {
                    if (z.get(0) instanceof String) {
                        flag = 1;
                        params.put("type", "List<String>");
                    }
                }
                if (flag == 0) {
                    listFlag = 1;
                    params.put("type", "List<Map<String,Object>>");
                }
                params.put("default", "new ArrayList<>()");
            } else if (tmp.get(y) instanceof Boolean) {
                params.put("type", "Boolean");
                params.put("default", false);
            } else if (tmp.get(y) instanceof Long) {
                params.put("type", "Long");
                params.put("default", -1L);
            } else if (tmp.get(y) instanceof Float || tmp.get(y) instanceof Double) {
                params.put("type", "Double");
                params.put("default", 0.0);
            } else {
//                System.out.println(y + "不存在" + tmp.get(y));
                params.put("type", "Object");
                params.put("default", "null");
            }
//            System.out.println();
            if (listFlag == 1) {
                ans.append(StringFormatUtils.formatByName(getListExchange(), params)).append("\n");
            } else if (mapFlag == 1) {
                ans.append(StringFormatUtils.formatByName(getMapExchange(), params)).append("\n");
            } else {
                ans.append(StringFormatUtils.formatByName(getExchange(), params)).append("\n");
            }
        }
        ans.append("return tmpDto;\n}");
        return ans.toString();
    }


    public static void getParams(Map<String, Object> tmp) {
        tmp = removeNull(tmp);
        Set<String> x = tmp.keySet();
        for (String y : x) {
            Map<String, Object> params = new HashMap<>();
            params.put("key", y);
            if (tmp.get(y) instanceof String) {
                System.out.println(StringFormatUtils.formatByName(strGetOrDefault(), params));
            } else if (tmp.get(y) instanceof Integer) {
                System.out.println(StringFormatUtils.formatByName(intGetOrDefault(), params));
            } else if (tmp.get(y) instanceof Map) {
                System.out.println(StringFormatUtils.formatByName(mapGetOrDefault(), params));
            } else if (tmp.get(y) instanceof List) {
                System.out.println(StringFormatUtils.formatByName(listGetOrDefault(), params));
            } else if (tmp.get(y) instanceof Boolean) {
                System.out.println(StringFormatUtils.formatByName(booleanGetOrDefault(), params));
            } else if (tmp.get(y) instanceof Long) {
                System.out.println(StringFormatUtils.formatByName(longGetOrDefault(), params));
            } else if (tmp.get(y) instanceof Float || tmp.get(y) instanceof Double) {
                System.out.println(StringFormatUtils.formatByName(doubleGetOrDefault(), params));
            } else {
                System.out.println(y + "不存在" + tmp.get(y));
                System.out.println(StringFormatUtils.formatByName(doubleGetOrDefault(), params));
            }
        }
    }

    private String getValue2Str(Object x) {
        if (Objects.isNull(x)) {
            return "";
        } else {
            return x.toString();
        }
    }


}
