package zerobase.stockinfo.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zerobase.stockinfo.model.Company;
import zerobase.stockinfo.model.ScrapedResult;
import zerobase.stockinfo.model.constants.CacheKey;
import zerobase.stockinfo.persist.CompanyRepository;
import zerobase.stockinfo.persist.DividendRepository;
import zerobase.stockinfo.persist.entity.CompanyEntity;
import zerobase.stockinfo.persist.entity.DividendEntity;
import zerobase.stockinfo.scraper.Scraper;

import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

  private final CompanyRepository companyRepository;
  private final DividendRepository dividendRepository;

  private final Scraper yahooFinanceScraper;

  @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
  @Scheduled(cron = "${scheduler.scrap.yahoo}")
  public void yahooFinanceScheduler(){
    log.info("Scraping Scheduler is started");
    //저장된 회사 목록 조회
    List<CompanyEntity> companies = companyRepository.findAll();

    //회사마다 배당금 정보를 새로 스크래핑
    for (var company : companies) {
      log.info("scraping scheduler is started -> " +company.getName());
      ScrapedResult scrapedResult =
              this.yahooFinanceScraper.scrap(Company.builder()
                                                    .name(company.getName())
                                                    .ticker(company.getTicker())
                                                    .build());

//스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
      scrapedResult.getDividends().stream()
              //디비든 모델을 디비든 엔티티로 매핑
              .map(e -> new DividendEntity(company.getId(), e))
              //엘리먼트를 하나씩 디비든 레파지토리에 삽입 (없는 경우에만)
              .forEach(e -> {
                boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                if (!exists) {
                  this.dividendRepository.save(e);
                }
              });
      //연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시 정지
      try {
        Thread.sleep(3000); //3초
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
