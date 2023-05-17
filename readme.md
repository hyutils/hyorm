# 基础文档
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
···java
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
···
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