# 基础文档
hyorm，一款自研java orm框架
## 使用简介
[项目地址](https://github.com/hyutils/hyorm)

[文档合集和示例地址](https://mp.weixin.qq.com/mp/appmsgalbum?__biz=MzI5MjY4OTQ2Nw==&action=getalbum&album_id=2954465444592091138&scene=173&from_msgid=2247484152&from_itemidx=1&count=3&nolastread=1#wechat_redirect)

使用方法：引入maven
```xml
<dependency>
        <groupId>com.hyutils</groupId>
        <artifactId>core</artifactId>
        <version>1.0-SNAPSHOT</version>
</dependency>
```
## 前提
所有的操作都需要创建一个简单Query对象，在并发环境中，请确保不同的查询语句创建不同的Query对象
```java
SimpleQuery query = new SimpleQuery(CrawlTaskHistory.class);
```
## 注意事项
本框架使用“约定大于配置”思想进行设计，所以有一些默认的数据库建表约定
1、删除标记和删除时间使用 deleted_mark/deleted_datetime
2、自增id为long型id，其他类型未进行测试

## 查询
以下方法均有各自情况下的重载方法
page方法
pageReturnListMap方法
findByCondition方法
findByConditionReturnListMap方法

### 简单的调用例子
```java
SimpleQuery query = new SimpleQuery(CrawlTaskHistory.class);
List<CrawlTaskHistory> ans = query.page(Json.toMap(Json.toJson(CrawlTaskHistory.builder().taskNameLike("数据库层测试").build())), 1, 10);
```
## 更新
updateById方法
update方法
updateByCondition方法

## 删除
delete方法
## 插入
insert方法
batchInsert方法

## where中字段支持的查询方式
查询操作首先使用一个hashmap作为入参（您可以使用简单对象，然后利用Json转map的方式转成map）
```java
Map<String,Object> condition = new HashMap<>();
Map<String,Object> condition = Json.toMap(Json.toJson(object));
```
### 字段的等值操作
设数据库字段名为 a，则在入参map中放入a的值则可以进行等值操作
```java
condition.put("a","1");
```
### 字段in操作
设数据库字段为a，则在map中放入 a_list即可以对字段进行in操作
```java
condition.put("a_list",new ArrayList<String>(){
            {
                add("1");
                add("2");
            }
        });
```
### 字段like操作
设数据库字段名为a，如果是左Like，则在字段值左边加上LikeParamExtension.PARAM_LEFT_LIKE；
如果是右Like，则在字段值右边加上LikeParamExtension.PARAM_RIGHT_LIKE；
```java
public Builder cronPeriodLike(String cronPeriod) {
            if (Objects.isNull(cronPeriod)) {
                this.target.setCronPeriod(cronPeriod);
                return this;
            }
            this.target.setCronPeriod(LikeParamExtension.PARAM_LEFT_LIKE + cronPeriod + LikeParamExtension.PARAM_RIGHT_LIKE);
            return this;
        }

public Builder cronPeriodLeftLike(String cronPeriod) {
            if (Objects.isNull(cronPeriod)) {
                this.target.setCronPeriod(cronPeriod);
                return this;
            }
            this.target.setCronPeriod(LikeParamExtension.PARAM_LEFT_LIKE + cronPeriod);
            return this;
        }

        public Builder cronPeriodRightLike(String cronPeriod) {
            if (Objects.isNull(cronPeriod)) {
                this.target.setCronPeriod(cronPeriod);
                return this;
            }
            this.target.setCronPeriod(cronPeriod + LikeParamExtension.PARAM_RIGHT_LIKE);
            return this;
        }
```
注意：like操作是改值而不是改字段的名字
### 字段不等于操作
设数据库字段为a，如果a为数字类型，则将该字段的值乘以-1即可
```
public Builder linkTypeNot(Integer linkType) {
            if (Objects.isNull(linkType)) {
                this.target.setLinkType(linkType);
                return this;
            }
            this.target.setLinkType(-1 * linkType);
            return this;
        }
```
如果a为字符串类型，则在值前面加上“-1*”
```java
public Builder nameNot(String name) {
            if (Objects.isNull(name)) {
                this.target.setName(name);
                return this;
            }
            this.target.setName("-1*" + name);
            return this;
        }
```
### 字段不在某个列表中(not in)
设数据库字段为a，则在字段的末尾加上_not_list即可
```java
condition.put("a_not_list",new ArrayList<String>(){
            {
                add("1");
                add("2");
            }
        });
```
### 时间类型字段的大于小于操作
设字段为a，如果是大于某个时间，则在a后面加上_bigger_than，如果是小于某个时间，则加上_lower_than
```java
condition.put("a_lower_than",LocalDateTime.now());
condition.put("a_bigger_than",LocalDateTime.now());
```
## 只查询某些字段的方式
在查询里面加入一个select_fields的字段，该字段是一个符合字段，结构如下
```java
public class SelectFields {
    private List<String> fields;

    public SelectFields() {
        this.fields = new ArrayList<>();
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public SelectFields select(String field){
        this.fields.add(field);
        return this;
    }
}
```
使用的方法为
```java
condition.put("select_fields",new SelectFields().select("a").select("b"));
```
## 查询时排序操作
在查询里面加入一个select_orders的字段，该字段是一个符合字段，结构如下
```java
public class SelectOrders {
    private List<SelectOrder> orders;

    public SelectOrders() {
        this.orders = new ArrayList<>();
    }

    public List<SelectOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<SelectOrder> orders) {
        this.orders = orders;
    }

    public SelectOrders then(String key, String order) {
        this.orders.add(new SelectOrder(key, order));
        return this;
    }
}
public class SelectOrder {
    private String key;
    private String order;

    public SelectOrder() {
    }

    public SelectOrder(String key, String order) {
        this.key = key;
        this.order = order;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
```
使用的方法为
```java
condition.put("select_orders",new SelectOrders()
                .then("max_reliability", "desc")
                .then("link_father", "desc")
                .then("link", "desc")
                .then("id", "desc"));
```
# 最佳实践
在上述的文档中，我们在使用的时候都是手动去创建和添加map里面的变量，这对于我们的使用来说都不太方便。基于此，框架也提供了基于简单对象的模式的方法。只需要在简单对象中生成getter、setter和builder方法，就可以直接将上述代码转化为java对象的编程方式。
```java
query.page(Json.toMap(Json.toJson(TaskBadMessage.builder()
                .selectFields(new SelectFields().select("id").select("task_id"))
                .selectOrders(new SelectOrders()
                        .then("modified_time","desc")
                        .then("created_time","desc"))
                .idList(new ArrayList<Long>(){
                    {
                        add(1L);
                        add(2L);
                        add(3L);
                    }
                })
                .idNot(1L)
                .pageTitleLike("测试")
                .contentNot("123")
                .createdTimeBiggerThan(LocalDateTime.now().minusMonths(2L))
                .build())),1,10);
```
最终便达到了可以使用直观的方式写结构化单表SQL的目的。
对于上述简单对象的生成，可以使用GenerateOrm对象里面的getSimpleModel生成model对象。从而快速进行使用

# 不带条件简单查询

## simpleGet方法
查询单个对象，如果有多个，只返回第一个
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery.find("value").find("id").find("thesaurus_code").size(1).simpleGet());
    }
```
生成的sql为：
```sql
SELECT value,id,thesaurus_code FROM material_thesaurus_words  limit 1 ;
```

## listMapGet方法
查询全部对象
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery.find("value").find("id").find("thesaurus_code").size(2).listMapGet());
    }
```
生成的sql为：
```sql
SELECT value,id,thesaurus_code FROM material_thesaurus_words  limit 2 ;
```
结果为：
```
[{value=xxxxxxxxx, id=585730307940941826, thesaurus_code=23}, {value=18&xxxxx, id=585730307940941827, thesaurus_code=23}]
```
## find方法
指查询某个字段
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery.find("value").find("id").find("thesaurus_code").size(1).simpleGet());
    }
``` 
生成的sql为：
```sql
SELECT value,id,thesaurus_code FROM material_thesaurus_words  limit 1 ;
```



## page方法和size方法
多少页，页的大小为多少，page方法需要和size一起使用，但是size方法可以单独使用
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery.find("value").find("id").find("thesaurus_code").page(1).size(1).simpleGet());
    }
```

生成的sql为
```sql
SELECT value,id,thesaurus_code FROM material_thesaurus_words  offset 0  limit 1 ;
```

## orderBy方法
按条件排序的方法
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery
                .find("value").find("id").find("thesaurus_code")
                .orderBy("thesaurus_code","desc").orderBy("value","desc")
                .page(1).size(2)
                .listMapGet());
    }
```
生成的sql为：
```sql
SELECT value,id,thesaurus_code FROM material_thesaurus_words order by thesaurus_code desc,value desc  offset 0  limit 2 ;
```

# 带条件查询
## where方法
该方法的入参是`WhereSyntaxTree`类，该类有两个继承类，分别是`AndWhereSyntaxTree`和`OrWhereSyntaxTree`，这两种不同的where树代表了两种不同的语义。

以下的内容均来源于上面两个语义类

## andWheres方法
AndWhereSyntaxTree语义，即内部的是用and连接起来的操作
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery
                .find("value").find("id").find("thesaurus_code")
                .orderBy("thesaurus_code","desc").orderBy("value","desc")
                .page(1).size(2)
                .andWheres(new HashMap<String, Object>(){
                    {
                        put("thesaurus_code","14");
                        put("value","demo123");
                    }
                })
                .listMapGet());
    }
```
生成的sql为：
```sql
SELECT value,id,thesaurus_code FROM material_thesaurus_words WHERE value = :value AND thesaurus_code = :thesaurus_code order by thesaurus_code desc,value desc  offset 0  limit 2 ;
```
参数为：
```
{value=demo123, thesaurus_code=14}
```
## orwhere方法
OrWhereSyntaxTree语义，即内部使用or连接
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery
                .find("value").find("id").find("thesaurus_code")
                .orderBy("thesaurus_code","desc").orderBy("value","desc")
                .page(1).size(2)
                .orWheres(new HashMap<String, Object>(){
                    {
                        put("thesaurus_code","14");
                        put("value","demo123");
                    }
                })
                .listMapGet());
    }
```
生成的sql为：
```sql
SELECT value,id,thesaurus_code FROM material_thesaurus_words WHERE value = :value OR thesaurus_code = :thesaurus_code order by thesaurus_code desc,value desc  offset 0  limit 2 ;
```

## 多个相同的参数or/and起来
由于hashmap本身是去重的，导致传入的时候无法做一个没有去重的参数，所以这里引入了andWheres和orWheres的重载方法，传入一个三元组。除了在这里可用之外，三元组还有别的用法，这里先介绍重复参数的用法。
### orwheres传入三元组
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery
                .find("value").find("id").find("thesaurus_code")
                .page(1).size(2)
                .orWheres(new ArrayList<Triplet<String, String, Object>>(){
                    {
                        add(new Triplet<>("thesaurus_code","=","99"));
                        add(new Triplet<>("thesaurus_code","=","14"));
                    }
                })
                .listMapGet());
    }
```
生成的sql为
```sql
SELECT value,id,thesaurus_code FROM material_thesaurus_words WHERE thesaurus_code = :thesaurus_code OR thesaurus_code = :a231e89bab64bed3388a6f9d0745be11  offset 0  limit 2 ;
```
参数为：
```
{thesaurus_code=99, 96000698b09c6b1afa517dd677fb90f5=14}
```
### andwheres传入三元组
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery
                .find("value").find("id").find("thesaurus_code")
                .page(1).size(2)
                .andWheres(new ArrayList<Triplet<String, String, Object>>(){
                    {
                        add(new Triplet<>("thesaurus_code","=","99"));
                        add(new Triplet<>("thesaurus_code","=","14"));
                    }
                })
                .listMapGet());
    }

```
生成的sql为：
```sql
SELECT value,id,thesaurus_code FROM material_thesaurus_words WHERE thesaurus_code = :thesaurus_code AND thesaurus_code = :5363ad8bf3fa098218eb28a15b2f3872  offset 0  limit 2 ;
```
参数为：
```
{5363ad8bf3fa098218eb28a15b2f3872=14, thesaurus_code=99}
```
## 组合使用
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery
                .find("value").find("id").find("thesaurus_code")
                .page(1).size(2)
                .orWheres(new ArrayList<Triplet<String, String, Object>>(){
                    {
                        add(new Triplet<>("thesaurus_code","=","99"));
                        add(new Triplet<>("thesaurus_code","=","14"));
                    }
                })
                .andWheres(new ArrayList<Triplet<String, String, Object>>(){
                    {
                        add(new Triplet<>("thesaurus_code","=","99"));
                        add(new Triplet<>("thesaurus_code","=","14"));
                    }
                })
                .listMapGet());
    }
```
生成的sql为
```sql
SELECT value, id, thesaurus_code
FROM material_thesaurus_words
WHERE thesaurus_code = :thesaurus_code
   OR thesaurus_code = :f6c7a5c9ac4f6fb2f77539b6b31c8f14 AND
      (thesaurus_code = :f65a9d7f4cafa174ce97dfb216321307 AND thesaurus_code = :27d714567083ce319446329df70ea1ae)
offset 0 limit 2;
```
参数为：
```
{f6c7a5c9ac4f6fb2f77539b6b31c8f14=14, 27d714567083ce319446329df70ea1ae=14, f65a9d7f4cafa174ce97dfb216321307=99, thesaurus_code=99}
```


## 多级参数
前面介绍的参数均出现在单层，所以在进行or操作的时候，如果再和别的条件进行and，可以发现其实生成的sql是有问题的。那么此时就需要引入多级参数了。

多级参数中，三元组的第三个参数可以传入一个`WhereSyntaxTree`对象，则这个对象作为二级参数存在。来看一个例子
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        WhereSyntaxTree whereSyntaxTree = new WhereSyntaxTree();
        System.out.println(wordsQuery
                .find("value").find("id").find("thesaurus_code")
                .page(1).size(2)
                .andWheres(new ArrayList<Triplet<String, String, Object>>(){
                    {
                        add(new Triplet<>("thesaurus_code","=", whereSyntaxTree.createOrTreeByOperate(new ArrayList<Triplet<String, String, Object>>(){
                            {
                                add(new Triplet<>("thesaurus_code","=","99"));
                                add(new Triplet<>("thesaurus_code","=","14"));
                            }
                        })));
                        add(new Triplet<>("这个随便写","=",whereSyntaxTree.createAndTreeByOperate(new ArrayList<Triplet<String, String, Object>>(){
                            {
                                add(new Triplet<>("value","=","22"));
                                add(new Triplet<>("value","=","11"));
                            }
                        })));
                        add(new Triplet<>("deleted_mark","=",false));
                    }
                })
                .listMapGet());
    }
```
生成的sql为：
```sql
SELECT value, id, thesaurus_code
FROM material_thesaurus_words
WHERE (thesaurus_code = :thesaurus_code OR thesaurus_code = :824abb38c3759e0a61a01f434237d6ed)
  AND (value = :value AND value = :c621bd6517d712bda0a05c313111ac67)
  AND deleted_mark = :deleted_mark
offset 0 limit 2;
```
参数为
```
{824abb38c3759e0a61a01f434237d6ed=14, deleted_mark=false, c621bd6517d712bda0a05c313111ac67=11, value=22, thesaurus_code=99}
```
可以看见，这次or语句就被括号包裹起来了。

# 条件不是等号的查询
## 利用三元组
三元组法上面已经介绍了，这里简单提供一些使用例子
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        WhereSyntaxTree whereSyntaxTree = new WhereSyntaxTree();
        System.out.println(wordsQuery
                .find("value").find("id").find("thesaurus_code")
                .page(1).size(2)
                .andWheres(new ArrayList<Triplet<String, String, Object>>(){
                    {
                        add(new Triplet<>("thesaurus_code","=", whereSyntaxTree.createOrTreeByOperate(new ArrayList<Triplet<String, String, Object>>(){
                            {
                                add(new Triplet<>("thesaurus_code","like","99%"));
                                add(new Triplet<>("thesaurus_code","like","14%"));
                            }
                        })));
                        add(new Triplet<>("这个随便写","=",whereSyntaxTree.createAndTreeByOperate(new ArrayList<Triplet<String, String, Object>>(){
                            {
                                add(new Triplet<>("value","!=","22"));
                                add(new Triplet<>("value","!=","11"));
                            }
                        })));
                        add(new Triplet<>("id",">",1L));
                        add(new Triplet<>("deleted_mark","=",false));
                    }
                })
                .listMapGet());
    }
```
生成的sql为：
```sql
SELECT value, id, thesaurus_code
FROM material_thesaurus_words
WHERE (thesaurus_code like :thesaurus_code OR thesaurus_code like :2cdde9107318c2af77faa6bc8d62df95)
  AND (value != :value AND value != :5408b2f7440d44234993b8ae44ae3860)
  AND id > :id
  AND deleted_mark = :deleted_mark
offset 0 limit 2;
```
除了like，数据库支持的其他简单操作也都能支持，只不过有一些特殊的函数需要手动编写SQL。下面简单介绍下编写SQL时，该框架的使用方法。

# 直接执行
## 获取列表
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery.totalSql("select thesaurus_code,count(id) from material_thesaurus_words group by thesaurus_code;").listMapGet());
}
```
输出结果为：
```sql
[{thesaurus_code=19, count=3772}, {thesaurus_code=23, count=6226}, {thesaurus_code=99, count=2}, {thesaurus_code=57, count=411}, {thesaurus_code=97, count=3353}, {thesaurus_code=12, count=2}, {thesaurus_code=27, count=3427}, {thesaurus_code=24, count=6}, {thesaurus_code=30, count=229}, {thesaurus_code=16, count=289}]
```

## 获取一个
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery.totalSql("select thesaurus_code,count(id) from material_thesaurus_words group by thesaurus_code order by count desc limit 1;").simpleGet());
}
```
执行的结果为：
```java
{thesaurus_code=23, count=6226}
```

## 带参数
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        System.out.println(wordsQuery.totalSql("select thesaurus_code,count(id) from material_thesaurus_words where deleted_mark = :deleted_mark group by thesaurus_code order by count desc;").addParams(new HashMap<String, Object>(){
            {
                put("deleted_mark",false);
            }
        }).listMapGet());
}
```
方法为addParams，用法与jdbc差不多。

# 更新数据
## update方法
与条件查询方法一样，update方法也可以传入hashmap和三元组列表两种，适用于不同的场景。
### 传入map
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        Map<String,Object> condition = wordsQuery.find("id").size(1).simpleGet();
        wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        wordsQuery.update(condition,new HashMap<String, Object>(){
            {
                put("deleted_mark",true);
            }
        });
}
```
生成的sql为：
```sql
UPDATE material_thesaurus_words SET  deleted_mark=:setdeleted_mark,modified_time=:setmodified_time WHERE id = :id ;
```
参数为：
```
{modified_time=2023-06-05T17:37:57, setmodified_time=2023-06-05T17:37:56.998, id=585730307940941826, setdeleted_mark=true}
```
其中modified_time为默认更新字段

### 传入三元组列表
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        Map<String,Object> condition = wordsQuery.find("id").size(1).simpleGet();
        wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        wordsQuery.update(new ArrayList<Triplet<String, String, Object>>(){
            {
                add(new Triplet<>("deleted_mark","!=",true));
            }
        },new HashMap<String, Object>(){
            {
                put("deleted_mark",true);
            }
        });
}
```
生成的sql是：
```sql
UPDATE material_thesaurus_words SET  deleted_mark=:setdeleted_mark,modified_time=:setmodified_time WHERE deleted_mark != :deleted_mark ;
```
# 保存数据
## insert方法
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        wordsQuery.insert(new HashMap<String, Object>(){
            {
                put("value","123");
                put("thesaurus_code","1");
            }
        });
}
```
生成的sql为：
```sql
INSERT INTO material_thesaurus_words(value,thesaurus_code) VALUES (:value,:thesaurus_code) RETURNING id;
```
## batchInsert方法
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);
        wordsQuery.batchInsert(new ArrayList<Map<String, Object>>(){
            {
                add(new HashMap<String, Object>(){
                    {
                        put("value","123");
                        put("thesaurus_code","1");
                    }
                });
                add(new HashMap<String, Object>(){
                    {
                        put("value","1234");
                        put("thesaurus_code","1");
                    }
                });
            }
        });
```
生成的sql为：
```sql
INSERT INTO material_thesaurus_words(value,thesaurus_code) VALUES (:value0,:thesaurus_code0),(:value1,:thesaurus_code1);
```
# 删除
## delete方法
按id删除
```java
    @Test
    public void demo2(){
        SimpleQuery wordsQuery = new SimpleQuery(MaterialThesaurusWords.class);

        wordsQuery.delete(wordsQuery.find("id").size(1).simpleGet().get("id"));
}
```
生成sql：
```sql
UPDATE material_thesaurus_words SET  deleted_mark=:setdeleted_mark,deleted_time=:setdeleted_time WHERE id = :1c950fec1d9bb9f2a2a14502fa82d6d8 ;
```