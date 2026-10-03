package com.ducky6464.mctiers.api;
import com.ducky6464.mctiers.cache.TierResult;
import java.net.URI;
import java.net.http.*;
import java.util.concurrent.*;
import java.util.regex.*;
public final class McTiersApi {
    private static final String BASE="https://tier-master-mace.lovable.app/api/public/v1/player/";
    private static final HttpClient HTTP=HttpClient.newHttpClient();
    private static final Pattern TIER=Pattern.compile("\\"tier\\"\\s*:\\s*\\"([^\\"]+)\\"");
    private static final Pattern LABEL=Pattern.compile("\\"label\\"\\s*:\\s*\\"([^\\"]+)\\"");
    private McTiersApi(){}
    public static CompletableFuture<TierResult> lookup(String username){
        if(!username.matches("[A-Za-z0-9_]{1,16}"))return CompletableFuture.completedFuture(null);
        HttpRequest q=HttpRequest.newBuilder(URI.create(BASE+username)).header("Accept","application/json").GET().build();
        return HTTP.sendAsync(q,HttpResponse.BodyHandlers.ofString()).thenApply(r->{
            if(r.statusCode()!=200||!r.body().contains("\"found\":true"))return null;
            Matcher t=TIER.matcher(r.body()),l=LABEL.matcher(r.body());
            if(!t.find())return null;
            return new TierResult(t.group(1),l.find()?l.group(1):"Tier",0);
        }).exceptionally(e->null);
    }
}
