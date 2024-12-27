package zerobase.stockinfo.scraper;

import zerobase.stockinfo.model.Company;
import zerobase.stockinfo.model.ScrapedResult;

public interface Scraper {
  Company scrapCompanyByTicker(String ticker);
  ScrapedResult scrap(Company company);
}
