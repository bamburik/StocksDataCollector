package org.bamburov.utils;

import java.util.List;

public class StatisticsUtils {

    public static double get10thPercentile(List<Double> list) {
        if (list.isEmpty()) {
            return 0;
        }
        if (list.size() < 10) {
            return list.get(0);
        }
        list = list.stream().sorted().toList();
        if (list.size() % 10 == 0) {
            return list.get((list.size() / 10) - 1);
        } else {
            double element1 = list.get((list.size() / 10) - 1);
            double element2 = list.get(list.size() / 10);
            return (element1 + element2) / 2;
        }
    }

    public static double get50thPercentile(List<Double> list) {
        if (list.isEmpty()) {
            return 0;
        }
        list = list.stream().sorted().toList();
        if (list.size() % 2 == 0) {
            double element1 = list.get((list.size() / 2) - 1);
            double element2 = list.get(list.size() / 2);
            return (element1 + element2) / 2;
        } else {
            return list.get(list.size() / 2);
        }
    }

    public static double get90thPercentile(List<Double> list) {
        if (list.isEmpty()) {
            return 0;
        }
        if (list.size() < 10) {
            return list.get(list.size() - 1);
        }
        list = list.stream().sorted().toList();
        if (list.size() % 10 == 0) {
            return list.get((list.size() - list.size() / 10) - 1);
        } else {
            double element1 = list.get((list.size() - list.size() / 10) - 1);
            double element2 = list.get(list.size() - list.size() / 10);
            return (element1 + element2) / 2;
        }
    }

}
