package org.bamburov.utils;

public class Constants {
    public static final String STOCK_INFO_URL_FORMAT = "https://tr-frontend-cdn.azureedge.net/bff/prod/stock/%s/payload.json?ver=1681572015328";
    public static final String GET_STOCKS_URL_FORMAT = "https://www.tipranks.com/api/Screener/GetStocks?break=1681738460916&page=%s";
    public static final String GET_TECHNICAL_ANALYSIS_URL_FORMAT = "https://www.tipranks.com/api/stocks/technicalAnalysis/?tickers=%s&timeFrame=%s";
    public static final String PRICE_AND_VOLUME_HISTORY_URL_FORMAT = "https://www.tipranks.com/api/stocks/getHistoricalPriceExtended?name=%s&daysBack=%s";



    public static final String TICKERS_JSONPATH = "data.ticker";
    public static final String TOTAL_ASSETS_JSONPATH = "financials.reports.balanceSheet.annual.data.findAll{ x -> x.name == 'TotalAssets'}[0].reportValues[0]";
    public static final String TOTAL_LIABILITIES_JSONPATH = "financials.reports.balanceSheet.annual.data.findAll{ x -> x.name == 'TotalLiabilities'}[0].reportValues[0]";
    public static final String TOTAL_REVENUE_JSONPATH = "financials.reports.incomeStatement.annual.data.findAll{ x -> x.name == 'TotalRevenue'}[0].reportValues[0]";
    public static final String SHARES_OUTSTANDING_JSONPATH = "common.stock.statistics.sharesOutstanding";


    public static final String SCORE_JSONPATH = "common.stock.smartScore.value";
    public static final String CURRENT_PRICE_JSONPATH = "common.stock.price";
    public static final String PRICE_TARGET_JSONPATH = "common.stock.analystRatings.consensus.priceTarget.value";
    public static final String BEST_PRICE_TARGET_JSONPATH = "common.stock.analystRatings.bestConsensus.priceTarget.value";
    public static final String ANALYST_CONSENSUS_JSONPATH = "common.stock.analystRatings.consensus.id";
    public static final String BEST_ANALYST_CONSENSUS_JSONPATH = "common.stock.analystRatings.bestConsensus.id";
    public static final String P2E_JSONPATH = "common.stock.pe";
    public static final String P2B_JSONPATH = "common.stock.statistics.priceToTangibleBookRatio";
    public static final String P2S_JSONPATH = "common.stock.statistics.priceToSalesRatio";
    public static final String P2FCF_JSONPATH = "common.stock.statistics.priceFreeCashFlowRatio";
    public static final String P2CF_JSONPATH = "common.stock.statistics.priceCashFlowRatio";
    public static final String EPS_JSONPATH = "common.stock.earning.espYear";
    public static final String BLOGGER_SENTIMENT_JSONPATH = "common.stock.bloggerOpinions.consensus.signal";
    public static final String HEDGE_FUND_TREND_JSONPATH = "common.stock.hedgeFundActivity.sentiment";
    public static final String BEST_CROWD_WISDOM_JSONPATH = "common.stock.investorActivity.best.sentiment";
    public static final String CROWD_WISDOM_JSONPATH = "common.stock.investorActivity.all.sentiment";
    public static final String NEWS_SENTIMENT_JSONPATH = "common.stock.newsSentiment.sentiment";
    public static final String TECHNICAL_SMA_JSONPATH = "common.stock.technical.sma";
    public static final String VOLUME = "common.stock.volume";
    public static final String TECHNICAL_IS_HAVE_DATA_FORMAT = "technical.isHaveData";
    public static final String TECHNICAL_DAY_SUMMARY_JSONPATH = "technical.day[0].technical.scores.summaryScore.scoreScale";
    public static final String TECHNICAL_DAY_OSCILLATORS_JSONPATH = "technical.day[0].technical.scores.oscillatorsScore.scoreScale";
    public static final String TECHNICAL_DAY_MOVING_JSONPATH = "technical.day[0].technical.scores.movingAveragesScore.scoreScale";
    public static final String TECHNICAL_SUMMARY_JSONPATH_FORMAT = "findAll { x -> x.ticker == '%S' }[0].scores.summaryScore.scoreScale";
    public static final String TECHNICAL_OSCILLATORS_JSONPATH_FORMAT = "findAll { x -> x.ticker == '%S' }[0].scores.oscillatorsScore.scoreScale";
    public static final String TECHNICAL_MOVING_JSONPATH_FORMAT = "findAll { x -> x.ticker == '%S' }[0].scores.movingAveragesScore.scoreScale";
    public static final String DAILY_INFO_FULL_DATE_BY_SHORT_DATE_FORMAT = "common.prices.data.find { x -> x.date =~ '%s' }.date";
    public static final String DAILY_INFO_INDEX_BY_FULL_DATE_FORMAT = "common.prices.data.stream().map { x -> x.date }.toList().indexOf('%s')";
    public static final String DAILY_INFO_PRICE_BY_INDEX_FORMAT = "common.prices.data[%s].price";
    public static final String DAILY_INFO_VOLUME_BY_INDEX_FORMAT = "common.prices.data[%s].volume";
    public static final String DAILY_INFO_OPEN_BY_INDEX_FORMAT = "common.prices.data[%s].open";
    public static final String DAILY_INFO_HIGH_BY_INDEX_FORMAT = "common.prices.data[%s].high";
    public static final String DAILY_INFO_LOW_BY_INDEX_FORMAT = "common.prices.data[%s].low";
    public static final String DAILY_INFO_CLOSE_BY_INDEX_FORMAT = "common.prices.data[%s].close";
    public static final String DAILY_INFO_DATE_BY_INDEX_FORMAT = "common.prices.data[%s].date";
    public static final String DAILY_INFO_LAST_INDEX_OF_PRICES_DATA = "common.prices.data.size()";

    public static final String PRICE_AND_VOLUME_HISTORY_FULL_DATE_BY_SHORT_DATE_FORMAT = "find { x -> x.date =~ '%s' }.date";
    public static final String PRICE_AND_VOLUME_HISTORY_INDEX_BY_FULL_DATE_FORMAT = "stream().map { x -> x.date }.toList().indexOf('%s')";
    public static final String PRICE_AND_VOLUME_HISTORY_PRICE_BY_INDEX_FORMAT = "[%s].price";
    public static final String PRICE_AND_VOLUME_HISTORY_OPEN_BY_INDEX_FORMAT = "[%s].open";
    public static final String PRICE_AND_VOLUME_HISTORY_HIGH_BY_INDEX_FORMAT = "[%s].high";
    public static final String PRICE_AND_VOLUME_HISTORY_LOW_BY_INDEX_FORMAT = "[%s].low";
    public static final String PRICE_AND_VOLUME_HISTORY_DATE_BY_INDEX_FORMAT = "[%s].date";
    public static final String PRICE_AND_VOLUME_HISTORY_VOLUME_BY_INDEX_FORMAT = "[%s].volume";
}
