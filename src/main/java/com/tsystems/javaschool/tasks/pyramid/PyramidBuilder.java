package com.tsystems.javaschool.tasks.pyramid;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PyramidBuilder {


    public int[][] buildPyramid(List<Integer> A) {
        int size = A.size();
        if (size > Integer.MAX_VALUE - 2) {
            throw new CannotBuildPyramidException();
        }
        //Проверка: можно ли из элементов предложенного листа сделать пирамиду
        int columns = 1;
        int rows = 1;
        int quantity = 0;
        while (quantity < size) {
            quantity = quantity + rows;
            rows++;
            columns = columns + 2;
        }
//Количество строк и столбцов, которое будет в пирамиде
        rows = rows - 1;
        columns = columns - 2;
        for (int i = 0; i < size; i++) {
            if (A.get(i) == null) {
                throw new CannotBuildPyramidException();
            }
        }
        if (quantity != size) {
            throw new CannotBuildPyramidException();
        }
        //Сортируем элементы списка по возрастанию
        Collections.sort(A);
        //Создаем матрицу, в которой мы будем строить пирамиду, и заполняем её нулями
        int[][] pyramid = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                pyramid[i][j] = 0;
            }
        }
        //Находим место, откуда начнется заполнение пирамиды
        int center = (columns / 2);
        //Задаем начальное количество элементов в первой строке
        int count = 1;
        //Задаем начальный сдвиг от центра
        int offset = 0;
//Задаем индекс первого элемента списка
        int listIndex = 0;
        for (int i = 0; i < rows; i++) {
            int start = center - offset;
            for (int j = 0; j < count * 2; j += 2) {
                pyramid[i][start + j] = A.get(listIndex);
                listIndex++;
            }
            offset++;
            count++;
        }
        //Вывод пирамиды
        return pyramid;


    }
}