package com.cardtransaction.service;

import com.cardtransaction.dto.ConvertedPurchaseResponse;
import com.cardtransaction.entity.PurchaseTransaction;
import com.cardtransaction.exception.CurrencyConversionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cardtransaction.config.TreasuryProperties;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
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

    private final TreasuryProperties treasuryProperties;

    public CurrencyConversionService(RestOperations restTemplate, TreasuryProperties treasuryProperties) {
        this.restTemplate = restTemplate;
        this.treasuryProperties = treasuryProperties;
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

            // Fetch response with retry - the fetchTreasuryResponse method is annotated with @Retryable
            String response = fetchTreasuryResponse(targetDate, currency);
            return parseExchangeRateResponse(response, targetDate, currency);
        } catch (RestClientException e) {
            logger.warn("Unable to retrieve exchange rate for {} on {}: {}", currency, targetDate, e.getMessage());
            throw new CurrencyConversionException("Unable to retrieve currency exchange rate due to service error", e);
        }
    }

    /**
     * Public retriable method that performs the HTTP GET to Treasury API. Annotated with @Retryable
     * to provide exponential backoff. Retries when RestClientException is thrown.
     */
    @Retryable(retryFor = { RestClientException.class }, maxAttempts = 4, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String fetchTreasuryResponse(LocalDate targetDate, String currency) {
        String apiUrl = buildTreasuryApiUrl(targetDate, currency);
        logger.info("Calling Treasury API: {}", apiUrl);

        try {
            return restTemplate.getForObject(apiUrl, String.class);
        } catch (HttpStatusCodeException hsce) {
            // Include the response body for debugging (truncated) but do not log at INFO level to avoid accidental exposure
            String respBody = hsce.getResponseBodyAsString();
            String snippet = respBody.length() > 1000 ? respBody.substring(0, 1000) + "...[truncated]" : respBody;
            logger.debug("Treasury API returned HTTP {}: {}", hsce.getStatusCode(), snippet);
            // Re-throw as RestClientException so retry mechanism handles it
            throw hsce;
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
        // rather than ISO codes like "EUR" or "INR". Use configured mappings (external.api.treasury.currency-mappings)
        // to translate ISO codes to dataset currency names. If no mapping exists, fall back to the provided value.
        String treasuryCurrency = treasuryProperties.mapIsoToTreasuryCurrency(currency);

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

            if (root.has("data") && root.get("data").isArray() && !root.get("data").isEmpty()) {
                JsonNode data = root.get("data").get(0);

                if (data.has("exchange_rate")) {
                    BigDecimal exchangeRate = new BigDecimal(data.get("exchange_rate").asText());
                    LocalDate effectiveDate = LocalDate.parse(
                            data.get("effective_date").asText(),
                            DateTimeFormatter.ISO_DATE);

                    return new ExchangeRateData(exchangeRate, effectiveDate);
                }
            }

            // No data available for requested filter
            logger.debug("Treasury API returned no data for currency {} and date {}. Raw response: {}", currency, targetDate,
                    response == null ? "(empty)" : (response.length() > 1000 ? response.substring(0, 1000) + "...[truncated]" : response));
            return null;
        } catch (Exception e) {
            String snippet = response == null ? "" : (response.length() > 1000 ? response.substring(0, 1000) + "...[truncated]" : response);
            logger.error("Error parsing exchange rate response for currency {} and date {}: {}", currency, targetDate, e.getMessage());
            logger.debug("Response body (truncated): {}", snippet);
            throw new CurrencyConversionException("Error parsing Treasury API response: " + (snippet.isEmpty() ? "(no response)" : snippet), e);
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
