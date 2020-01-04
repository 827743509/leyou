package com.leyou.common.pojo;


import java.util.List;

public class PageResult<T> {
    private Long total;
    private  Long tatalPage;
    private List<T> items;

    public PageResult() {
    }

    public PageResult(Long total, Long tatalPage, List<T> items) {
        this.total = total;
        this.tatalPage = tatalPage;
        this.items = items;
    }

    public PageResult(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getTatalPage() {
        return tatalPage;
    }

    public void setTatalPage(Long tatalPage) {
        this.tatalPage = tatalPage;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
