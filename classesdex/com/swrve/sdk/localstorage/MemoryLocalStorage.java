package com.swrve.sdk.localstorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class MemoryLocalStorage implements ILocalStorage, IFlushableLocalStorage {
    private static final int MAX_ELEMENTS = 2000;
    private List<StoredEvent> events = new ArrayList();
    private Map<String, StoredCacheEntry> serverCache = new HashMap();
    private List<StoredClickThru> clickThrus = new ArrayList();

    private static class StoredEvent {
        public static long eventCount = 0;
        public String event;
        public long id;

        public StoredEvent() {
            long j = eventCount;
            eventCount = 1 + j;
            this.id = j;
        }
    }

    private static class StoredCacheEntry {
        public String category;
        public String rawData;
        public String userId;

        private StoredCacheEntry() {
        }
    }

    private static class StoredClickThru {
        public static long clickThruCount = 0;
        public long id;
        public String source;
        public int targetGameId;

        public StoredClickThru() {
            long j = clickThruCount;
            clickThruCount = 1 + j;
            this.id = j;
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public synchronized void addEvent(String eventJSON) throws Exception {
        if (this.events.size() < 2000) {
            StoredEvent newEvent = new StoredEvent();
            newEvent.event = eventJSON;
            this.events.add(newEvent);
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public synchronized void removeEventsById(Collection<Long> ids) {
        Iterator<StoredEvent> iter = this.events.iterator();
        while (iter.hasNext()) {
            StoredEvent event = iter.next();
            if (ids.contains(Long.valueOf(event.id))) {
                iter.remove();
            }
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public synchronized Map<Long, String> getFirstNEvents(Integer n) {
        Map<Long, String> topEvents;
        topEvents = new HashMap<>();
        Iterator<StoredEvent> iter = this.events.iterator();
        for (int countLeft = n.intValue(); iter.hasNext() && countLeft > 0; countLeft--) {
            StoredEvent event = iter.next();
            topEvents.put(Long.valueOf(event.id), event.event);
        }
        return topEvents;
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public synchronized String getCacheEntryForUser(String userId, String category) {
        StoredCacheEntry foundEntry;
        String uniqueId = userId + "##" + category;
        foundEntry = this.serverCache.get(uniqueId);
        return foundEntry != null ? foundEntry.rawData : null;
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public synchronized void setCacheEntryForUser(String userId, String category, String rawData) {
        String uniqueId = userId + "##" + category;
        StoredCacheEntry savedEntry = this.serverCache.get(uniqueId);
        if (savedEntry == null && this.serverCache.size() < 2000) {
            savedEntry = new StoredCacheEntry();
            this.serverCache.put(uniqueId, savedEntry);
        }
        savedEntry.userId = userId;
        savedEntry.category = category;
        savedEntry.rawData = rawData;
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public synchronized void addClickThru(int targetGameId, String source) {
        if (this.clickThrus.size() < 2000) {
            StoredClickThru clickThru = new StoredClickThru();
            clickThru.targetGameId = targetGameId;
            clickThru.source = source;
            this.clickThrus.add(clickThru);
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public synchronized void removeClickThrusById(long id) {
        Iterator<StoredClickThru> iter = this.clickThrus.iterator();
        while (iter.hasNext()) {
            StoredClickThru clickThru = iter.next();
            if (id == clickThru.id) {
                iter.remove();
            }
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public synchronized Map<Long, Map.Entry<Integer, String>> getFirstNClickThrus(Integer n) {
        Map<Long, Map.Entry<Integer, String>> topClickThrus;
        topClickThrus = new HashMap<>();
        Iterator<StoredClickThru> iter = this.clickThrus.iterator();
        for (int countLeft = n.intValue(); iter.hasNext() && countLeft > 0; countLeft--) {
            StoredClickThru clickThru = iter.next();
            topClickThrus.put(Long.valueOf(clickThru.id), new SimpleEntry<>(Integer.valueOf(clickThru.targetGameId), clickThru.source));
        }
        return topClickThrus;
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public synchronized Map<Map.Entry<String, String>, String> getAllCacheEntries() {
        Map<Map.Entry<String, String>, String> allCacheEntries;
        allCacheEntries = new HashMap<>();
        for (String key : this.serverCache.keySet()) {
            StoredCacheEntry entry = this.serverCache.get(key);
            allCacheEntries.put(new SimpleEntry<>(entry.userId, entry.category), entry.rawData);
        }
        return allCacheEntries;
    }

    @Override // com.swrve.sdk.localstorage.IFlushableLocalStorage
    public synchronized void flushEvents(IFastInsertLocalStorage externalStorage) {
        List<String> eventsToFlush = new ArrayList<>();
        for (StoredEvent event : this.events) {
            eventsToFlush.add(event.event);
        }
        externalStorage.addMultipleEvent(eventsToFlush);
        this.events.clear();
    }

    @Override // com.swrve.sdk.localstorage.IFlushableLocalStorage
    public synchronized void flushCache(IFastInsertLocalStorage externalStorage) {
        Iterator<String> cacheIter = this.serverCache.keySet().iterator();
        List<Map.Entry<String, Map.Entry<String, String>>> cacheEntries = new ArrayList<>();
        while (cacheIter.hasNext()) {
            StoredCacheEntry cacheEntry = this.serverCache.get(cacheIter.next());
            cacheEntries.add(new SimpleEntry<>(cacheEntry.userId, new SimpleEntry(cacheEntry.category, cacheEntry.rawData)));
        }
        externalStorage.setMultipleCacheEntries(cacheEntries);
        this.serverCache.clear();
    }

    @Override // com.swrve.sdk.localstorage.IFlushableLocalStorage
    public synchronized void flushClickThrus(IFastInsertLocalStorage externalStorage) {
        List<Map.Entry<Integer, String>> clickThrusToFlush = new ArrayList<>();
        for (StoredClickThru clickThru : this.clickThrus) {
            clickThrusToFlush.add(new SimpleEntry<>(Integer.valueOf(clickThru.targetGameId), clickThru.source));
        }
        externalStorage.addMultipleClickThrus(clickThrusToFlush);
        this.clickThrus.clear();
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void close() {
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void reset() {
        this.events.clear();
        this.serverCache.clear();
        this.clickThrus.clear();
    }
}
