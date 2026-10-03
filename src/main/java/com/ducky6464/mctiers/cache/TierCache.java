package com.ducky6464.mctiers.cache;
import com.ducky6464.mctiers.api.McTiersApi;
import java.util.Map;
import java.util.concurrent.*;
public final class TierCache {
    private static final long TTL=600000L;
    private final Map<String,TierResult> values=new ConcurrentHashMap<>();
    private final Map<String,CompletableFuture<TierResult>> pending=new ConcurrentHashMap<>();
    public CompletableFuture<TierResult> get(String username){
        String key=username.toLowerCase();
        TierResult c=values.get(key);
        if(c!=null&&!c.expired()) return CompletableFuture.completedFuture(c);
        return pending.computeIfAbsent(key,k->McTiersApi.lookup(username)
            .thenApply(r->{if(r!=null)values.put(key,new TierResult(r.tier(),r.mode(),System.currentTimeMillis()+TTL));return r;})
            .whenComplete((r,e)->pending.remove(key)));
    }
    public TierResult getCached(String username){
        TierResult value = values.get(username.toLowerCase());
        return value != null && !value.expired() ? value : null;
    }
    public void clear(){values.clear();pending.clear();}
}
