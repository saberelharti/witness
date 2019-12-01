package net.dm73.plainpress.util;

import net.dm73.plainpress.model.Event;

import java.util.Comparator;

public class PublishedTimeBasedComparator implements Comparator<Event> {

    @Override
    public int compare(Event firstEvent, Event secondEvent) {
        if((long)firstEvent.getPublishedAt()<(long)secondEvent.getPublishedAt())
            return 1;
        else if((long)firstEvent.getPublishedAt()==(long)secondEvent.getPublishedAt())
            return 0;
        else
            return -1;
    }
}
