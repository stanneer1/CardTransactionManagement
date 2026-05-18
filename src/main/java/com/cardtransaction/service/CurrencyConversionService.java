package com.cardtransaction.service;

import com.cardtransaction.dto.ConvertedPurchaseResponse;
import com.cardtransaction.entity.PurchaseTransaction;
import com.cardtransaction.exception.CurrencyConversionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CurrencyConversionService {

    private final RestOperations restTemplate;
    private static final String TREASURY_API_URL = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange";
    private static final int MONTHS_LOOKBACK = 6;
    private static final String USD = "USD";

    private static final Logger logger = LoggerFactory.getLogger(CurrencyConversionService.class);

    public CurrencyConversionService(RestOperations restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Convert a purchase transaction to a target currency using Treasury API exchange rates
     * @param transaction the purchase transaction to convert
     * @param targetCurrency the target currency code (e.g., "EUR", "GBP")
     * @return the converted purchase response
     * @throws CurrencyConversionException if conversion cannot be completed
     */
    public ConvertedPurchaseResponse convertPurchase(PurchaseTransaction transaction, String targetCurrency) {
        return convertPurchase(transaction, USD, targetCurrency);
    }

    /**
     * Convert a purchase transaction from source currency to target currency
     * @param transaction the purchase transaction to convert
     * @param sourceCurrency the source currency code (default USD)
     * @param targetCurrency the target currency code
     * @return the converted purchase response
     * @throws CurrencyConversionException if conversion cannot be completed
     */
    public ConvertedPurchaseResponse convertPurchase(PurchaseTransaction transaction, String sourceCurrency, String targetCurrency) {
        if (sourceCurrency == null || sourceCurrency.trim().isEmpty()) {
            throw new CurrencyConversionException("Source currency cannot be null or empty");
        }
        if (targetCurrency == null || targetCurrency.trim().isEmpty()) {
            throw new CurrencyConversionException("Target currency cannot be null or empty");
        }
        if (sourceCurrency.equals(targetCurrency)) {
            throw new CurrencyConversionException("Source and target currencies cannot be the same");
        }

        try {
            // Get exchange rate for the purchase date
            ExchangeRateData exchangeRateData = getExchangeRate(transaction.getTransactionDate(), targetCurrency);

            if (exchangeRateData == null) {
                throw new CurrencyConversionException(
                        "No currency conversion rate available for " + targetCurrency +
                        " on or before " + transaction.getTransactionDate() +
                        " within the last 6 months");
            }

            // Convert the amount
            BigDecimal convertedAmount = transaction.getPurchaseAmount()
                    .multiply(exchangeRateData.getExchangeRate())
                    .setScale(2, RoundingMode.HALF_UP);

            return ConvertedPurchaseResponse.builder()
                    .id(transaction.getId())
                    .description(transaction.getDescription())
                    .transactionDate(transaction.getTransactionDate())
                    .originalPurchaseAmount(transaction.getPurchaseAmount())
                    .originalCurrency(sourceCurrency)
                    .targetCurrency(targetCurrency)
                    .exchangeRate(exchangeRateData.getExchangeRate())
                    .convertedAmount(convertedAmount)
                    .exchangeRateDate(exchangeRateData.getExchangeRateDate())
                    .build();
        } catch (RestClientException e) {
            logger.error("Error calling Treasury API: " + e.getMessage(), e);
            throw new CurrencyConversionException(
                    "Unable to retrieve currency exchange rate due to service error", e);
        }
    }

    /**
     * Get the exchange rate for a specific date and currency
     * @param targetDate the date for which to get the exchange rate
     * @param currency the target currency code
     * @return the exchange rate data, or null if not found
     */
    private ExchangeRateData getExchangeRate(LocalDate targetDate, String currency) {
        try {
            // For a production system, we would call the Treasury API and parse the results
            // For testing purposes, we'll return a mock implementation that can be tested
            // In reality, the API endpoint would be called with proper parameters

            String apiUrl = buildTreasuryApiUrl(targetDate, currency);
            logger.info("Calling Treasury API: " + apiUrl);

            String response = restTemplate.getForObject(apiUrl, String.class);
            return parseExchangeRateResponse(response, targetDate, currency);
        } catch (RestClientException e) {
            logger.warn("Unable to retrieve exchange rate for " + currency + " on " + targetDate + ": " + e.getMessage());
            throw new CurrencyConversionException(
                    "Unable to retrieve currency exchange rate due to service error", e);
        }
    }

    /**
     * Build the Treasury API URL with filters for the specified date and currency
     * @param targetDate the date for which to get the exchange rate
     * @param currency the target currency code
     * @return the formatted API URL
     */
    private String buildTreasuryApiUrl(LocalDate targetDate, String currency) {
        LocalDate sixMonthsAgo = targetDate.minusMonths(MONTHS_LOOKBACK);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // The Treasury rates_of_exchange dataset uses the field name `currency` (full currency name)
        // rather than ISO codes like "EUR" or "INR". Map common ISO currency codes to the
        // dataset's currency names (e.g. INR -> Rupee, EUR -> Euro). If no mapping exists,
        // fall back to the provided value.
        String treasuryCurrency = mapIsoToTreasuryCurrency(currency);

        // Build the URL using UriComponentsBuilder to ensure proper encoding of query params
        String filterValue = String.format("currency:eq:%s,effective_date:gte:%s,effective_date:lte:%s",
                treasuryCurrency, sixMonthsAgo.format(formatter), targetDate.format(formatter));

        return org.springframework.web.util.UriComponentsBuilder.fromHttpUrl(TREASURY_API_URL)
                .queryParam("filter", filterValue)
                .queryParam("sort", "-effective_date")
                .queryParam("limit", 1)
                .toUriString();
    }

    /**
     * Map ISO currency codes to the Treasury dataset's currency names.
     * This is a small mapping for common currencies used by the application.
     */
    private String mapIsoToTreasuryCurrency(String iso) {
        if (iso == null) return "";
        String code = iso.trim().toUpperCase();
        return switch (code) {
            case "USD" -> "Dollar";
            case "EUR" -> "Euro";
            case "GBP" -> "Pound";
            case "INR" -> "Rupee";
            case "JPY" -> "Yen";
            case "AUD" -> "Dollar"; // Australia-Dollar entries exist under 'Dollar'
            case "CAD" -> "Dollar"; // Canada-Dollar
            case "CHF" -> "Franc";
            case "CNY" -> "Yuan";
            default -> iso;
        };
    }

    /**
     * Parse the Treasury API response to extract exchange rate data
     * @param response the API response JSON
     * @param targetDate the date for which the rate applies
     * @param currency the currency code
     * @return the exchange rate data
     */
    private ExchangeRateData parseExchangeRateResponse(String response, LocalDate targetDate, String currency) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            if (root.has("data") && root.get("data").isArray() && root.get("data").size() > 0) {
                JsonNode data = root.get("data").get(0);

                if (data.has("exchange_rate")) {
                    BigDecimal exchangeRate = new BigDecimal(data.get("exchange_rate").asText());
                    LocalDate effectiveDate = LocalDate.parse(
                            data.get("effective_date").asText(),
                            DateTimeFormatter.ISO_DATE);

                    return new ExchangeRateData(exchangeRate, effectiveDate);
                }
            }

            return null;
        } catch (Exception e) {
            logger.error("Error parsing exchange rate response: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Inner class to hold exchange rate data
     */
    private static class ExchangeRateData {
        private final BigDecimal exchangeRate;
        private final LocalDate exchangeRateDate;

        public ExchangeRateData(BigDecimal exchangeRate, LocalDate exchangeRateDate) {
            this.exchangeRate = exchangeRate;
            this.exchangeRateDate = exchangeRateDate;
        }

        public BigDecimal getExchangeRate() {
            return exchangeRate;
        }

        public LocalDate getExchangeRateDate() {
            return exchangeRateDate;
        }
    }
}
