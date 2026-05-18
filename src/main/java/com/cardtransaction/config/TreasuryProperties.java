package com.cardtransaction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "external.api.treasury")
public class TreasuryProperties {

    /** Map of ISO currency codes to the Treasury dataset currency name (e.g. INR -> Rupee) */
    private Map<String, String> currencyMappings = new HashMap<>();

    private String baseUrl;

    public Map<String, String> getCurrencyMappings() {
        return currencyMappings;
    }

    public void setCurrencyMappings(Map<String, String> currencyMappings) {
        this.currencyMappings = currencyMappings;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String mapIsoToTreasuryCurrency(String iso) {
        if (iso == null) return "";
        String key = iso.trim().toUpperCase();
        return currencyMappings.getOrDefault(key, iso);
    }
}

