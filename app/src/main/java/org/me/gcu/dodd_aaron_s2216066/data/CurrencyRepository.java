package org.me.gcu.dodd_aaron_s2216066.data;

import android.util.Log;

import org.me.gcu.dodd_aaron_s2216066.model.CurrencyRate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class CurrencyRepository {

    private static final String TAG = "CurrencyRepository";

    public CurrencyRepository() {
    }

    public String downloadRss(String urlString) {
        StringBuilder builder = new StringBuilder();
        URL aurl;
        URLConnection yc;
        BufferedReader in = null;
        String inputLine;

        Log.d(TAG, "downloadRss: starting download for " + urlString);

        try {
            aurl = new URL(urlString);
            yc = aurl.openConnection();
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
        } catch (IOException e) {
            Log.e(TAG, "downloadRss: IOException", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException closeEx) {
                    Log.e(TAG, "downloadRss: error closing stream", closeEx);
                }
            }
        }

        String result = builder.toString();
        Log.d(TAG, "downloadRss: finished, length=" + result.length());
        return result;
    }
    public String cleanRssXml(String rawXml) {
        if (rawXml == null || rawXml.isEmpty()) {
            return rawXml;
        }

        String cleaned = rawXml;

        int i = cleaned.indexOf("<?");
        if (i >= 0) {
            cleaned = cleaned.substring(i);
        }

        i = cleaned.indexOf("</rss>");
        if (i >= 0) {
            cleaned = cleaned.substring(0, i + 6);
        }

        Log.d(TAG, "cleanRssXml: cleaned length=" + cleaned.length());
        return cleaned;
    }

    public List<CurrencyRate> parseRssToRates(String cleanedXml) {
        List<CurrencyRate> rates = new ArrayList<>();

        if (cleanedXml == null || cleanedXml.isEmpty()) {
            Log.e(TAG, "parseRssToRates: empty XML");
            return rates;
        }

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(cleanedXml));

            int eventType = xpp.getEventType();
            CurrencyRate currentRate = null;
            String currentTag = null;
            String channelPubDate = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = xpp.getName();

                        if ("item".equalsIgnoreCase(currentTag)) {
                            currentRate = new CurrencyRate();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        String text = xpp.getText();
                        if (text != null) {
                            text = text.trim();
                        }
                        if (text == null || text.isEmpty()) {
                            break;
                        }

                        if ("lastBuildDate".equalsIgnoreCase(currentTag)) {
                            channelPubDate = text;
                        } else if (currentRate != null) {
                            if ("title".equalsIgnoreCase(currentTag)) {
                                currentRate.setTitle(text);

                                int open = text.lastIndexOf('(');
                                int close = text.lastIndexOf(')');
                                if (open >= 0 && close > open) {
                                    String code = text.substring(open + 1, close).trim();
                                    currentRate.setCode(code);
                                }
                            } else if ("pubDate".equalsIgnoreCase(currentTag)) {
                                currentRate.setPubDate(text);
                            } else if ("description".equalsIgnoreCase(currentTag)) {
                                currentRate.setRate(extractRateFromDescription(text));
                                extractNameAndCountryFromDescription(text, currentRate);
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        String endTag = xpp.getName();
                        if ("item".equalsIgnoreCase(endTag) && currentRate != null) {
                            if (currentRate.getPubDate() == null && channelPubDate != null) {
                                currentRate.setPubDate(channelPubDate);
                            }
                            rates.add(currentRate);
                            currentRate = null;
                        }
                        currentTag = null;
                        break;

                    default:
                        break;
                }

                eventType = xpp.next();
            }

            Log.d(TAG, "parseRssToRates: parsed " + rates.size() + " items");

        } catch (XmlPullParserException e) {
            Log.e(TAG, "parseRssToRates: XmlPullParserException", e);
        } catch (IOException e) {
            Log.e(TAG, "parseRssToRates: IOException", e);
        }

        return rates;
    }

    private double extractRateFromDescription(String description) {
        if (description == null) {
            return 0.0;
        }
        String[] parts = description.split("=");
        if (parts.length < 2) {
            return 0.0;
        }
        String right = parts[1].trim();
        String[] tokens = right.split(" ");
        for (String token : tokens) {
            try {
                return Double.parseDouble(token);
            } catch (NumberFormatException ignore) {
            }
        }
        return 0.0;
    }

    private void extractNameAndCountryFromDescription(String description, CurrencyRate rate) {
        if (description == null || rate == null) {
            return;
        }
        String[] parts = description.split("=");
        if (parts.length < 2) {
            return;
        }
        String right = parts[1].trim();
        String[] tokens = right.split(" ");
        if (tokens.length < 2) {
            return;
        }

        int startIndex = 0;
        for (int i = 0; i < tokens.length; i++) {
            try {
                Double.parseDouble(tokens[i]);
                startIndex = i + 1;
                break;
            } catch (NumberFormatException ignore) {
            }
        }
        if (startIndex >= tokens.length) {
            return;
        }

        StringBuilder nameBuilder = new StringBuilder();
        for (int i = startIndex; i < tokens.length; i++) {
            if (i > startIndex) {
                nameBuilder.append(" ");
            }
            nameBuilder.append(tokens[i]);
        }
        String fullName = nameBuilder.toString().trim();
        rate.setName(fullName);

        String[] nameTokens = fullName.split(" ");
        if (nameTokens.length > 1) {
            StringBuilder countryBuilder = new StringBuilder();
            for (int i = 0; i < nameTokens.length - 1; i++) {
                if (i > 0) {
                    countryBuilder.append(" ");
                }
                countryBuilder.append(nameTokens[i]);
            }
            rate.setCountry(countryBuilder.toString().trim());
        } else {
            rate.setCountry(fullName);
        }
    }

    public RssResult downloadAndParse(String urlString) {
        String raw = downloadRss(urlString);
        String cleaned = cleanRssXml(raw);
        List<CurrencyRate> list = parseRssToRates(cleaned);
        return new RssResult(raw, cleaned, list);
    }

    public static class RssResult {
        public final String rawXml;
        public final String cleanedXml;
        public final List<CurrencyRate> rates;

        public RssResult(String rawXml, String cleanedXml, List<CurrencyRate> rates) {
            this.rawXml = rawXml;
            this.cleanedXml = cleanedXml;
            this.rates = rates;
        }
    }
}
