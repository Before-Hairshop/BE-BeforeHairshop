package com.beforehairshop.demo.hairdesigner.handler;

public class PageOffsetHandler {
    public Integer getOffsetByPageNumber(Integer pageNumber) {
        if (pageNumber <= 0) {
            return 0;
        } else {                        // 2 이상은 빼기 1해주고, * 5 해준다.
            return pageNumber * 5;
        }
    }
}
