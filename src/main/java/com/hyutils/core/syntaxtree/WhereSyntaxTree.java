package com.hyutils.core.syntaxtree;


import com.hyutils.core.utils.ArrayStrUtil;
import com.hyutils.core.utils.MD5Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javatuples.Triplet;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

public class WhereSyntaxTree {

    public Boolean isFinal = false;
    List<WhereSyntaxTree> childTree = new ArrayList<>();
    public WhereSyntaxNode whereSyntaxNode;
    private Log logger = LogFactory.getLog(WhereSyntaxTree.class);


    public List<WhereSyntaxTree> getChildTree() {
        return childTree;
    }

    public String getSql(Map<String, Object> params) {
        if (isFinal) {
            if (params.containsKey(whereSyntaxNode.getSetName())) {
                Random random = new Random();
                whereSyntaxNode.setSetName(MD5Utils.compMd5(whereSyntaxNode.getSetName() + LocalDateTime.now().toString() + random.ints().toString()));
            }
            params.put(whereSyntaxNode.getSetName(), whereSyntaxNode.getValue());
            if (whereSyntaxNode.getValueContainBracket()) {
                return whereSyntaxNode.getName() + " " + whereSyntaxNode.getOperate() + " (:" + whereSyntaxNode.getSetName() + ")";
            } else {
                return whereSyntaxNode.getName() + " " + whereSyntaxNode.getOperate() + " :" + whereSyntaxNode.getSetName();
            }
        } else {
            String sunSql = "";
            // TODO: 2021/8/16 如果是一棵子树
            for (WhereSyntaxTree whereSyntaxTree : childTree) {
                if (whereSyntaxTree instanceof AndWhereSyntaxTree) {
                    if (sunSql.equals("")) {
                        sunSql = whereSyntaxTree.getSql(params);
                    } else {
                        sunSql = sunSql + " AND " + whereSyntaxTree.getSql(params);
                    }
                } else if (whereSyntaxTree instanceof OrWhereSyntaxTree) {
                    if (sunSql.equals("")) {
                        sunSql = whereSyntaxTree.getSql(params);
                    } else {
                        sunSql = sunSql + " OR " + whereSyntaxTree.getSql(params);
                    }
                }
            }
            if (StringUtils.hasText(sunSql)) {
                return "(" + sunSql + ")";
            } else {
                return "";
            }
        }
    }


    public OrWhereSyntaxTree createFinalOrTree(String name, Object value) {
        OrWhereSyntaxTree whereSyntaxTree = new OrWhereSyntaxTree();
        if (value instanceof WhereSyntaxTree) {
            if (((WhereSyntaxTree) value).childTree.size() == 0) {
                whereSyntaxTree = null;
            } else {
                whereSyntaxTree.isFinal = false;
                whereSyntaxTree.childTree = ((WhereSyntaxTree) value).childTree;
            }
        } else if (value instanceof List) {
            whereSyntaxTree.isFinal = true;
            whereSyntaxTree.whereSyntaxNode = new WhereSyntaxNode(name, "in", true, value);
        } else {
            whereSyntaxTree.isFinal = true;
            whereSyntaxTree.whereSyntaxNode = new WhereSyntaxNode(name, "=", value);
        }
        return whereSyntaxTree;
    }

    public OrWhereSyntaxTree createFinalOrTreeByOperate(String name, String operate, Object value) {
        OrWhereSyntaxTree whereSyntaxTree = new OrWhereSyntaxTree();
        if (value instanceof WhereSyntaxTree) {
            if (((WhereSyntaxTree) value).childTree.size() == 0) {
                whereSyntaxTree = null;
            } else {
                whereSyntaxTree.isFinal = false;
                whereSyntaxTree.childTree = ((WhereSyntaxTree) value).childTree;
            }
        } else if (value instanceof List) {
            whereSyntaxTree.isFinal = true;
            whereSyntaxTree.whereSyntaxNode = new WhereSyntaxNode(name, "in", true, value);
        } else {
            whereSyntaxTree.isFinal = true;
            // TODO: 2021/12/28 如果value是list，可能需要将operate改为in
            whereSyntaxTree.whereSyntaxNode = new WhereSyntaxNode(name, operate, value);
        }
        return whereSyntaxTree;
    }

    public AndWhereSyntaxTree createFinalAndTree(String name, Object value) {
        AndWhereSyntaxTree whereSyntaxTree = new AndWhereSyntaxTree();
        if (value instanceof WhereSyntaxTree) {
            if (((WhereSyntaxTree) value).childTree.size() == 0) {
                whereSyntaxTree = null;
            } else {
                whereSyntaxTree.isFinal = false;
                whereSyntaxTree.childTree = ((WhereSyntaxTree) value).childTree;
            }
        } else if (value instanceof List) {
            whereSyntaxTree.isFinal = true;
            whereSyntaxTree.whereSyntaxNode = new WhereSyntaxNode(name, "in", true, value);
        } else {
            whereSyntaxTree.isFinal = true;
            whereSyntaxTree.whereSyntaxNode = new WhereSyntaxNode(name, "=", value);
        }
        return whereSyntaxTree;
    }


    public AndWhereSyntaxTree createFinalAndTreeByOperate(String name, String operate, Object value) {
        AndWhereSyntaxTree whereSyntaxTree = new AndWhereSyntaxTree();
        if (value instanceof WhereSyntaxTree) {
            if (((WhereSyntaxTree) value).childTree.size() == 0) {
                whereSyntaxTree = null;
            } else {
                whereSyntaxTree.isFinal = false;
                whereSyntaxTree.childTree = ((WhereSyntaxTree) value).childTree;
            }
        } else if (value instanceof List) {
            whereSyntaxTree.isFinal = true;
            whereSyntaxTree.whereSyntaxNode = new WhereSyntaxNode(name, "in", true, value);
        } else {
            whereSyntaxTree.isFinal = true;
            // TODO: 2021/12/28 如果value是list，可能需要将operate改为in
            whereSyntaxTree.whereSyntaxNode = new WhereSyntaxNode(name, operate, value);
        }
        return whereSyntaxTree;
    }

    /**
     * 获取全量操作树
     *
     * @param name
     * @param operate
     * @param value
     * @return
     */
    public WhereSyntaxTree createFinalTreeByOperate(String name, String operate, Object value) {
        WhereSyntaxTree whereSyntaxTree = null;
        if (value instanceof WhereSyntaxTree) {
            if (((WhereSyntaxTree) value).childTree.size() == 0) {
                whereSyntaxTree = null;
            } else {
                whereSyntaxTree = new AndWhereSyntaxTree();
                whereSyntaxTree.isFinal = false;
                whereSyntaxTree.childTree = ((WhereSyntaxTree) value).childTree;
            }
        } else if (value instanceof List) {
            int flag = 0;
            if (((List) value).size() > 0) {
                if (((List) value).get(0) instanceof List) {
                    flag = 1;
                    whereSyntaxTree = new AndWhereSyntaxTree();
                    whereSyntaxTree.isFinal = false;
                    whereSyntaxTree.childTree = new ArrayList<>();
                    int n = ((List) value).size();
                    for (int i = 0; i < n; i++) {
                        whereSyntaxTree.childTree.add(createFinalTreeByOperate(name, operate, ((List) value).get(i)));
                    }
                }
                if (((List) value).get(0) instanceof Triplet) {
                    flag = 1;
                    whereSyntaxTree = new AndWhereSyntaxTree();
                    whereSyntaxTree.isFinal = false;
                    whereSyntaxTree.childTree = new ArrayList<>();
                    int n = ((List) value).size();
                    for (int i = 0; i < n; i++) {
                        whereSyntaxTree.childTree.add(createFinalTreeByOperate(name, operate, ((List) value).get(i)));
                    }
                }
            }
            if (flag == 0) {
                whereSyntaxTree = new AndWhereSyntaxTree();
                whereSyntaxTree.isFinal = true;
                whereSyntaxTree.whereSyntaxNode = new WhereSyntaxNode(name, operate, true, value);
            }
        } else if (value instanceof Triplet) {
            whereSyntaxTree = new OrWhereSyntaxTree();
            whereSyntaxTree.isFinal = true;
            whereSyntaxTree.whereSyntaxNode = new WhereSyntaxNode(((Triplet) value).getValue0().toString(),((Triplet) value).getValue1().toString(),((Triplet) value).getValue2());
        } else {
            whereSyntaxTree = new AndWhereSyntaxTree();
            whereSyntaxTree.isFinal = true;
            // TODO: 2021/12/28 如果value是list，可能需要将operate改为in
            whereSyntaxTree.whereSyntaxNode = new WhereSyntaxNode(name, operate, value);
        }
        return whereSyntaxTree;

    }

    public OrWhereSyntaxTree createOrTree(Map<String, Object> orWheres) {
        OrWhereSyntaxTree whereSyntaxTree = new OrWhereSyntaxTree();
        whereSyntaxTree.isFinal = false;
        for (Map.Entry<String, Object> x : orWheres.entrySet()) {
            whereSyntaxTree.childTree.add(createFinalOrTree(x.getKey(), x.getValue()));
        }
        return whereSyntaxTree;
    }

    public OrWhereSyntaxTree createOrTreeByOperate(List<Triplet<String, String, Object>> orWheres) {
        OrWhereSyntaxTree whereSyntaxTree = new OrWhereSyntaxTree();
        whereSyntaxTree.isFinal = false;
        for (Triplet<String, String, Object> orWhere : orWheres) {
            whereSyntaxTree.childTree.add(createFinalOrTreeByOperate(orWhere.getValue0(), orWhere.getValue1(),
                    orWhere.getValue2()));
        }
        return whereSyntaxTree;
    }


    public AndWhereSyntaxTree createAndTree(Map<String, Object> andWheres) {
        AndWhereSyntaxTree whereSyntaxTree = new AndWhereSyntaxTree();
        whereSyntaxTree.isFinal = false;
        for (Map.Entry<String, Object> x : andWheres.entrySet()) {
            WhereSyntaxTree whereSyntaxTree1 = createFinalAndTree(x.getKey(), x.getValue());
            if (Objects.nonNull(whereSyntaxTree1)) {
                whereSyntaxTree.childTree.add(whereSyntaxTree1);
            }
        }
        return whereSyntaxTree;
    }

    public AndWhereSyntaxTree createAndTreeByOperate(List<Triplet<String, String, Object>> andWheres) {
        AndWhereSyntaxTree whereSyntaxTree = new AndWhereSyntaxTree();
        whereSyntaxTree.isFinal = false;
        for (Triplet<String, String, Object> andWhere : andWheres) {
//            WhereSyntaxTree whereSyntaxTree1 = createFinalAndTreeByOperate(andWhere.getValue0(), andWhere.getValue1(), andWhere.getValue2());
            WhereSyntaxTree whereSyntaxTree1 = createFinalTreeByOperate(andWhere.getValue0(), andWhere.getValue1(), andWhere.getValue2());
            if (Objects.nonNull(whereSyntaxTree1)) {
                whereSyntaxTree.childTree.add(whereSyntaxTree1);
            }
        }
        return whereSyntaxTree;
    }
}
