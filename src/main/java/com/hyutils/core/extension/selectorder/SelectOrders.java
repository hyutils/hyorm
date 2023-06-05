package com.hyutils.core.extension.selectorder;

import com.hyutils.core.utils.StringFormatUtils;

import javax.validation.constraints.Max;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 用于排序
 */
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
