package com.beforehairshop.demo.hairdesigner.handler;

public class PageOffsetHandler {
    public Integer getOffsetByPageNumber(Integer pageNumber) {
        if (pageNumber == 1) {
            return 0;
        } else if (pageNumber < 1) {    // 1 미만은 첫 번째 페이지
            return 0;
        } else {                        // 2 이상은 빼기 1해주고, * 5 해준다.
            return (pageNumber - 1) * 5;
        }
    }
}
