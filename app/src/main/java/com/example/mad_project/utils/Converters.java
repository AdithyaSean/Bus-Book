package com.example.mad_project.utils;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.List;

public class Converters {
    @TypeConverter
    public String fromList(List<String> list) {
        return String.join(",", list);
    }

    @TypeConverter
    public List<String> fromString(String value) {
        return Arrays.asList(value.split(","));
    }
}