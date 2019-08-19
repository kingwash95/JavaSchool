package com.tsystems.javaschool.tasks.subsequence;

import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Subsequence {


    public static boolean find(List<Object> X, List<Object> Y) {
        int XIdx = 0;
        int YIdx = 0;
        if (X == null || Y == null) {
            throw new IllegalArgumentException();
        }
        if (X.size() > Y.size()) {
            return false;
        }
        while (XIdx < X.size()) {
            Object XElem = X.get(XIdx);
            Object YElem = Y.get(YIdx);
            while (!XElem.equals(YElem)) {
                if (++YIdx > Y.size() - 1) {
                    return false;
                }
                YElem = Y.get(YIdx);
            }
            XIdx++;
        }
        return true;
    }
}