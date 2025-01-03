package zerobase.stockinfo.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import zerobase.stockinfo.model.Company;
import zerobase.stockinfo.model.Dividend;
import zerobase.stockinfo.model.ScrapedResult;
import zerobase.stockinfo.model.constants.Month;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {

  private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history/" +
          "?frequency=1mo&period1=%d&period2=%d";
  private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s";

  private static final long START_TIME = 86400;

  @Override
  public ScrapedResult scrap(Company company) {
    var scrapedResult = new ScrapedResult();
    scrapedResult.setCompany(company);

    try {
      long now = System.currentTimeMillis() / 1000;
      String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
      Connection connection = Jsoup.connect(url);
      Document document = connection.get();

      Elements parsingDivs = document.getElementsByClass("table yf-j5d1ld noDl");
      Element tableEle = parsingDivs.first(); // get(0)도 가능
      Element tbody = tableEle.children().get(1);

      List<Dividend> dividends = new ArrayList<>();
      for (Element e: tbody.children()){
        String txt = e.text();
        if (!txt.endsWith("Dividend")){
          continue;
        }

        String[] split = txt.split(" ");
        int month = Month.strToNumber(split[0]);
        int day = Integer.parseInt(split[1].replace(",", ""));
        int year = Integer.parseInt(split[2]);
        String dividend = split[3];

        if (month < 0){
          throw new RuntimeException("Unexpected month enum value -> " + split[0]);
        }
        dividends.add(Dividend.builder()
                .date(LocalDateTime.of(year, month, day, 0, 0))
                .dividend(dividend)
                .build());
      }
      scrapedResult.setDividends(dividends);

    }catch (Exception e) {
      e.printStackTrace();
    }

    return scrapedResult;
  }

  @Override
  public Company scrapCompanyByTicker(String ticker) {
    String url = String.format(SUMMARY_URL, ticker);

    try {
      Document document = Jsoup.connect(url).get();
      String title = document.getElementsByTag("h1").get(1).text();
      System.out.println(title);
      int num = title.indexOf("(");
      title = title.substring(0, num-1);

      return Company.builder()
              .ticker(ticker)
              .name(title)
              .build();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
