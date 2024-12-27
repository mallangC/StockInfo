package zerobase.stockinfo;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import zerobase.stockinfo.model.Company;
import zerobase.stockinfo.scraper.YahooFinanceScraper;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class StockInfoApplication {

  public static void main(String[] args) {
    SpringApplication.run(StockInfoApplication.class, args);
  }
}
