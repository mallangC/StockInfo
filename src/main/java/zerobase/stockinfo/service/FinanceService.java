package zerobase.stockinfo.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import zerobase.stockinfo.model.Company;
import zerobase.stockinfo.model.Dividend;
import zerobase.stockinfo.model.ScrapedResult;
import zerobase.stockinfo.model.constants.CacheKey;
import zerobase.stockinfo.persist.CompanyRepository;
import zerobase.stockinfo.persist.DividendRepository;
import zerobase.stockinfo.persist.entity.CompanyEntity;
import zerobase.stockinfo.persist.entity.DividendEntity;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

  private final CompanyRepository companyRepository;
  private final DividendRepository dividendRepository;

  //캐시가 없을 때는 아래 코드를 실행하고 있을 때는 캐시에서 찾아서 준다
  @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
  public ScrapedResult getDividendByCompanyName(String companyName){
    log.info("search company -> "+ companyName);
    //1. 회사명을 기준으로 회사 정보를 조회
    CompanyEntity company = this.companyRepository.findByName(companyName)
            .orElseThrow(() -> new RuntimeException("company not found"));

    //2. 조회된 회사 정보(ID)로 배당금을 조회
    List<DividendEntity> dividendEntities = this.dividendRepository
            .findAllByCompanyId(company.getId());

    //3. 결과 조합 후 변환
    List<Dividend> dividends = new ArrayList<>();
    for(var entity : dividendEntities){
      dividends.add(Dividend.builder()
              .date(entity.getDate())
              .dividend(entity.getDividend())
              .build());
    }
//
//    List<Dividend> dividends = dividendEntities.stream()
//            .map(e -> Dividend.builder()
//                    .date(e.getDate())
//                    .dividend(e.getDividend())
//                    .build())
//            .toList();

    return new ScrapedResult(Company.builder()
                                    .name(company.getName())
                                    .ticker(company.getTicker())
                                    .build(), dividends);
  }
}
