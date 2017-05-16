//
// Created by levin on 17-5-12.
//
#pragma once

#include <cstddef>

template<typename T>
struct Array {
    int length;
    T* data;

    Array () {
        length = 0;
        data = NULL;
    }

    Array (int s) {
        length = s;
        data = new T[length];
    }

    void create(int size) {
        if (data)
            delete[] data;
        length = size;
        if (length > 0)
            data = new T[length];
    }

    void set(int elem, T val) {
        data[elem] = val;
    }

    T& get(int pos) {
        return data[pos];
    }

    T& operator[](int pos) {
        return data[pos];
    }
};
