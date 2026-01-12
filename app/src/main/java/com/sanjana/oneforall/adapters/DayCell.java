package com.sanjana.oneforall.adapters;

import com.sanjana.oneforall.database.CalendarEvent;
import java.util.ArrayList;
import java.util.List;

public class DayCell {
    public int day;
    public String date;
    public List<CalendarEvent> events = new ArrayList<>();

    public DayCell(int day) {
        this.day = day;
    }
}
