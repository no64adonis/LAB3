package com.lottery.service;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CardValidationServiceImpl implements CardValidationService {
    private static final Logger logger = Logger.getLogger(CardValidationServiceImpl.class.getName());
    private static final String BIN_API_URL = "https://lookup.binlist.net/";

    
    public boolean isValidCard(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }
        
        
        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        
        if (cleanNumber.length() < 13 || cleanNumber.length() > 19) {
            return false;
        }

        
        if (!passesLuhnCheck(cleanNumber)) {
            logger.warning("Card failed Luhn algorithm check.");
            return false;
        }

        
        
        String bin = cleanNumber.substring(0, 6);
        return isBinReal(bin);
    }

    private boolean passesLuhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private boolean isBinReal(String bin) {
        try {
            URL url = new URL(BIN_API_URL + bin);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept-Version", "3");
            connection.setConnectTimeout(5000); 
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            
            
            
            
            if (responseCode == 200) {
                return true;
            } else if (responseCode == 404) {
                logger.warning("BIN " + bin + " not found on third-party API.");
                return false;
            } else {
                logger.warning("BIN API returned unexpected code: " + responseCode + ". Failing open.");
                
                
                return true;
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error connecting to BIN validation API: " + e.getMessage(), e);
            
            return true;
        }
    }
}
