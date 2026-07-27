package com.swrve.sdk.localstorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class MemoryCachedLocalStorage implements ILocalStorage {
    private ILocalStorage cache;
    private ILocalStorage secondaryStorage;
    private Object eventLock = new Object();
    private Object cacheLock = new Object();
    private Object clickThruLock = new Object();

    public MemoryCachedLocalStorage(ILocalStorage cache, ILocalStorage secondaryStorage) {
        this.cache = cache;
        this.secondaryStorage = secondaryStorage;
    }

    public void setSecondaryStorage(ILocalStorage secondaryStorage) {
        this.secondaryStorage = secondaryStorage;
    }

    public ILocalStorage getSecondaryStorage() {
        return this.secondaryStorage;
    }

    public void setCacheStorage(ILocalStorage cacheStorage) {
        this.cache = cacheStorage;
    }

    public ILocalStorage getCacheStorage() {
        return this.cache;
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public String getCacheEntryForUser(String userId, String category) {
        String result;
        synchronized (this.cacheLock) {
            result = this.cache.getCacheEntryForUser(userId, category);
            if (result == null && this.secondaryStorage != null) {
                result = this.secondaryStorage.getCacheEntryForUser(userId, category);
            }
        }
        return result;
    }

    public Map<ILocalStorage, Map<Long, String>> getCombinedFirstNEvents(Integer n) {
        Map<ILocalStorage, Map<Long, String>> result;
        Map<Long, String> events;
        synchronized (this.eventLock) {
            result = new HashMap<>();
            int eventCount = 0;
            if (this.secondaryStorage != null && (eventCount = (events = this.secondaryStorage.getFirstNEvents(n)).size()) > 0) {
                result.put(this.secondaryStorage, events);
            }
            if (n.intValue() - eventCount > 0) {
                Map<Long, String> events2 = this.cache.getFirstNEvents(Integer.valueOf(n.intValue() - eventCount));
                int remainingEventCount = events2.size();
                if (remainingEventCount > 0) {
                    result.put(this.cache, events2);
                }
            }
        }
        return result;
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void addEvent(String eventJSON) throws Exception {
        synchronized (this.eventLock) {
            this.cache.addEvent(eventJSON);
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void removeEventsById(Collection<Long> ids) {
        synchronized (this.eventLock) {
            this.cache.removeEventsById(ids);
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public Map<Long, String> getFirstNEvents(Integer ids) {
        Map<Long, String> firstNEvents;
        synchronized (this.eventLock) {
            firstNEvents = this.cache.getFirstNEvents(ids);
        }
        return firstNEvents;
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void setCacheEntryForUser(String userId, String category, String rawData) {
        synchronized (this.cacheLock) {
            this.cache.setCacheEntryForUser(userId, category, rawData);
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void addClickThru(int targetGameId, String source) {
        synchronized (this.clickThruLock) {
            this.cache.addClickThru(targetGameId, source);
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void removeClickThrusById(long id) {
        synchronized (this.clickThruLock) {
            this.cache.removeClickThrusById(id);
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public Map<Long, Map.Entry<Integer, String>> getFirstNClickThrus(Integer n) {
        Map<Long, Map.Entry<Integer, String>> firstNClickThrus;
        synchronized (this.clickThruLock) {
            firstNClickThrus = this.cache.getFirstNClickThrus(n);
        }
        return firstNClickThrus;
    }

    public Map<ILocalStorage, Map<Long, Map.Entry<Integer, String>>> getCombinedFirstNClickThrus(Integer n) {
        Map<ILocalStorage, Map<Long, Map.Entry<Integer, String>>> result;
        Map<Long, Map.Entry<Integer, String>> events;
        synchronized (this.clickThruLock) {
            result = new HashMap<>();
            int eventCount = 0;
            if (this.secondaryStorage != null && (eventCount = (events = this.secondaryStorage.getFirstNClickThrus(n)).size()) > 0) {
                result.put(this.secondaryStorage, events);
            }
            if (n.intValue() - eventCount > 0) {
                Map<Long, Map.Entry<Integer, String>> events2 = this.cache.getFirstNClickThrus(Integer.valueOf(n.intValue() - eventCount));
                int remainingEventCount = events2.size();
                if (remainingEventCount > 0) {
                    result.put(this.cache, events2);
                }
            }
        }
        return result;
    }

    public void flush() throws Exception {
        if (this.cache != this.secondaryStorage && (this.cache instanceof IFlushableLocalStorage) && (this.secondaryStorage instanceof IFastInsertLocalStorage)) {
            IFlushableLocalStorage flushableStorage = (IFlushableLocalStorage) this.cache;
            IFastInsertLocalStorage targetStorage = (IFastInsertLocalStorage) this.secondaryStorage;
            synchronized (this.eventLock) {
                flushableStorage.flushEvents(targetStorage);
            }
            synchronized (this.cacheLock) {
                flushableStorage.flushCache(targetStorage);
            }
            synchronized (this.clickThruLock) {
                flushableStorage.flushClickThrus(targetStorage);
            }
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public Map<Map.Entry<String, String>, String> getAllCacheEntries() {
        return this.cache.getAllCacheEntries();
    }

    public Map<Map.Entry<String, String>, String> getCombinedCacheEntries() {
        Map<Map.Entry<String, String>, String> result = this.cache.getAllCacheEntries();
        if (this.secondaryStorage != null) {
            result.putAll(this.secondaryStorage.getAllCacheEntries());
        }
        return result;
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void close() {
        this.cache.close();
        if (this.secondaryStorage != null) {
            this.secondaryStorage.close();
        }
    }

    @Override // com.swrve.sdk.localstorage.ILocalStorage
    public void reset() {
        this.cache.reset();
        if (this.secondaryStorage != null) {
            this.secondaryStorage.reset();
        }
    }
}
