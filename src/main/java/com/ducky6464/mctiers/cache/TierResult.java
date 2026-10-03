package com.ducky6464.mctiers.cache;
public record TierResult(String tier, String mode, long expiresAt) {
    public boolean expired(){ return System.currentTimeMillis() >= expiresAt; }
    public String label(){ return tier + " " + mode; }
}
